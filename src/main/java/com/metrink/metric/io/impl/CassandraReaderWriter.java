package com.metrink.metric.io.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.metric.io.MetricReaderWriter;
import com.metrink.utils.MilliSecondUtils;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ByteBufferRange;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.AbstractSerializer;
import com.netflix.astyanax.serializers.AnnotatedCompositeSerializer;
import com.netflix.astyanax.serializers.AsciiSerializer;
import com.netflix.astyanax.util.RangeBuilder;

/**
 * Fetcher that pulls metrics from the Cassandra cluster.
 *
 * The fetcher uses the client from Netflix: https://github.com/Netflix/astyanax
 */
@Singleton
public class CassandraReaderWriter implements MetricReaderWriter {

    public static final Logger LOG = LoggerFactory.getLogger(CassandraReaderWriter.class);

    private final AstyanaxContext<Keyspace> context;
    private final ColumnFamily<MetricRowKey, Long> columnFamily;
    private final MetricMetadata metadata;

    /**
     * Initialize the {@link MetricReaderWriter}.
     * @param context Cassandra context used to query metrics
     * @param columnFamily column family of the metrics
     * @param metadata delegate for obtaining metric meta-data (e.g., unique devices, groups, etc)
     */
    @Inject
    public CassandraReaderWriter(final AstyanaxContext<Keyspace> context,
                                 final ColumnFamily<MetricRowKey, Long> columnFamily,
                                 final MetricMetadata metadata) {
        this.context = context;
        this.columnFamily = columnFamily;
        this.metadata = metadata;
    }

    @Override
    public void init() {
        LOG.info("Initializing cassandra reader/writer");
        context.start();
        metadata.init();
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down cassandra reader/writer");
        context.shutdown();
        metadata.shutdown();
    }

    @Override
    public Map<MetricId, MetricValueList> readMetrics(final List<MetricRequest> requests) {
        final Map<MetricId, MetricValueList> results = new HashMap<MetricId, MetricValueList>();

        for (final MetricRequest request : requests) {
            final MetricValueList values = new MetricValueList(request.getStart(), request.getEnd());

            long start = 0, end;

            if(LOG.isDebugEnabled()) {
                LOG.debug("Loading metrics from cassandra: {}", request.getId());
                LOG.debug("Getting metrics: {} -> {}", request.getStart(), request.getEnd());
                start = System.currentTimeMillis();
            }

            for (final String yearMonth : MilliSecondUtils.generateYearMonthSet(request.getStart(), request.getEnd())) {
                readMetricsByYearMonth(request, values, yearMonth);
            }

            if(LOG.isDebugEnabled()) {
                end = System.currentTimeMillis();
                LOG.debug("Fetched {} metrics in {}ms", values.size(), (end-start));
            }

            results.put(request.getId(), values);
        }

        return results;
    }

    @Override
    public MetricValueList readMetrics(final MetricId id, final long start, final long end) {
        return readMetrics(Arrays.asList(new MetricRequest(id, start, end))).get(id);
    }

    @Override
    public void writeMetrics(final List<Metric> metrics) {
        final Set<MetricId> metricIds = Sets.newHashSet();
        final MutationBatch mutation = context.getClient().prepareMutationBatch();

        for (final Metric metric : metrics) {
            final MetricRowKey metricRowKey = new MetricRowKey(metric.getId(), MilliSecondUtils.millisToYearMonth(metric.getTimestamp()));

            LOG.trace("Writing metric: {} to {}", metric, metricRowKey);

            metricIds.add(metric.getId());

            mutation.withRow(columnFamily, metricRowKey)
                .putColumn(metric.getTimestamp(), metric.getValue(), null);
        }

        LOG.debug("Wrote {} metrics", metrics.size());

        /*
         * I think we want to "commit" the Cassandra data before the MySQL data.
         */
        try {
            mutation.execute();
        } catch (final ConnectionException e) {
            LOG.error("Cassandra connection exception: {}", e.getMessage(), e);
        }

        if (!metadata.writeMetricIds(metricIds)) {
            LOG.error("Failed to persist metric metadata");
            return;
        }
    }

    @Override
    public int deleteMetrics(final long before) {
        // go back one year, which should cover everything
        final long start = new DateTime(before).minusYears(1).getMillis();
        final SortedSet<String> yearMonthRange = MilliSecondUtils.generateYearMonthSet(start, before);

        final MutationBatch mutation = context.getClient().prepareMutationBatch();

        int count = 0; // this count is kinda BS

        // need to read in all (dev, group, name) tuples first
        for(final MetricId id:metadata.readUniqueMetrics()) {
            for(final String yearMonth:yearMonthRange) {
                final MetricRowKey metricRowKey = new MetricRowKey(id, yearMonth);
                mutation.withRow(columnFamily, metricRowKey).delete();
                count++;
            }
        }
        try {
            mutation.execute();
        } catch (final ConnectionException e) {
            LOG.error("Cassandra connection exception: {}", e.getMessage(), e);
            return 0;
        }

        return count;
    }

    /**
     * Read the metrics for a given year.
     *
     * NOTE: This does not perform bounds checking on the start and end of the {@link MetricRequest}.
     *
     * @param request the metric request
     * @param values the value list to populate
     * @param year the year to query
     */
    private void readMetricsByYearMonth(final MetricRequest request, final MetricValueList values, final String yearMonth) {
        final MetricRowKey metricRowKey = new MetricRowKey(request.getId(), yearMonth);

        try {
            final ByteBufferRange range = new RangeBuilder()
                .setStart(request.getStart())
                .setEnd(request.getEnd())
                //.setMaxSize() // TODO: Do we want to set an upper limit?
                .build();

            final ColumnList<Long> searchResults = context.getClient()
                    .prepareQuery(columnFamily)
                    .getKey(metricRowKey)
                    .withColumnRange(range)
                    .execute().getResult();

            for (final Column<Long> result : searchResults) {
                // later we might want result.getDoubleValue to actually return JSON or some packaging of value & unit
                values.addMetric(new Metric(request.getId(), result.getName(), result.getDoubleValue(), null));
            }

        } catch (final ConnectionException e) {
            LOG.error("Cassandra connection exception: {}", e.getMessage(), e);
        }
    }

    /**
     * Class used to identify a row key.
     */
    public static class MetricRowKey {
        private String yearMonth;
        private String device;
        private String group;
        private String name;

        /**
         * Initialize the instance.
         * @param yearMonth YYYYMM
         * @param device the device
         * @param group the group
         * @param name the name
         */
        public MetricRowKey(final String yearMonth,
                            final String device,
                            final String group,
                            final String name) {
            this.yearMonth = yearMonth;
            this.device = device;
            this.group = group;
            this.name = name;

            checkArgument(-1 == device.indexOf(':'), "Device not found: %s", device);
            checkArgument(-1 == group.indexOf(':'), "Group not found: %s", group);
            checkArgument(-1 == name.indexOf(':'), "Name not found: %s", name);
            checkArgument(yearMonth.length() == 6, "Year month length not 6: %s", yearMonth);
        }

        /**
         * Initialize the instance.
         * @param metricId the metric id
         * @param yearMonth YYYYMM
         */
        public MetricRowKey(final MetricId metricId, final String yearMonth) {
            this(yearMonth,
                 metricId.getDevice(),
                 metricId.getGroupName(),
                 metricId.getName());
        }

        @Override
        public String toString() {
            return new StringBuffer()
                .append(yearMonth).append(':')
                .append(device).append(':')
                .append(group).append(':')
                .append(name)
                .toString();
        }

        /**
         * Helper method not actually utilized by Cassandra.
         * @param rowKey the string row key
         * @return the row key
         */
        public static MetricRowKey of(final String rowKey) {
            final String[] split = rowKey.split(":");

            checkArgument(split.length == 6, "Row key doesn't match the Metric Id format: {}", rowKey);

            return new MetricRowKey(
                        split[2],  // YYYYMM
                        split[3],  // device
                        split[4],  // group
                        split[5]); // name
        }
    }

    /**
     * Custom serializer for converting to and from {@link MetricRowKey}s. It serializes to ASCII format. We can always
     * later utilize utf-8 if so desired..
     *
     * Started by using {@link AnnotatedCompositeSerializer}, however this serializes into a binary format. We probably
     * won't be happy with that, particularly if we every want to query using the CLI utility.
     */
    public static class MetricRowIdSerializer extends AbstractSerializer<MetricRowKey> {

        @Override
        public ByteBuffer toByteBuffer(final MetricRowKey obj) {
            return AsciiSerializer.get().toByteBuffer(obj.toString());
        }

        @Override
        public MetricRowKey fromByteBuffer(final ByteBuffer byteBuffer) {
            return MetricRowKey.of(AsciiSerializer.get().fromByteBuffer(byteBuffer));
        }
    }
}
