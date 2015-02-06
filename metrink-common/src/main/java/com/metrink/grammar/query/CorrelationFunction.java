package com.metrink.grammar.query;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.metric.io.MetricReader;
import com.metrink.metric.io.MetricReaderWriter;
import com.metrink.stats.Correlation;

/**
 * Find metrics owned by the same {@link MetricOwner} which have a correlation to the selected metrics.
 */
public class CorrelationFunction extends QueryFunction {

    public static final Logger LOG = LoggerFactory.getLogger(CorrelationFunction.class);

    private final Correlation correlation;
    private final MetricReaderWriter metricReader;
    private final MetricMetadata metricMetadata;

    /**
     * Initialize the CorrelationFuction.
     * @param correlation Correlation instance used to correlate values.
     * @param metricReader Metric reader used to look-up other metrics
     * @param metricMetadata Metric metadata used to obtain a full list of metrics.
     */
    @Inject
    public CorrelationFunction(final Correlation correlation,
                               final MetricReaderWriter metricReader,
                               final MetricMetadata metricMetadata) {
        this.correlation = correlation;
        this.metricReader = metricReader;
        this.metricMetadata = metricMetadata;
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start,
                                                  final long end,
                                                  final ImmutableMap<MetricId, MetricValueList> context)
            throws MetrinkParseException {

        if (context.size() == 0) {
            LOG.warn("Context contained no metrics - shortcircuiting correlation function");
            return context;

        } else if (end - start > TimeUnit.MINUTES.toMillis(31)) {
            // TODO: Why does it need to be 31 to make -30m work?
            LOG.warn("Time range exceeded thirty minutes: {}", (end-start));

            throw new MetrinkParseException("Time span too large, please limit to 30 minutes when computing correlations");
        }

        final Map<MetricId, MetricValueList> correlationCandidates = getCorrelationCandidates(start, end, context);
        final Map<MetricId, MetricValueList> ret = Maps.newHashMap();

        for(final Map.Entry<MetricId, MetricValueList> result : context.entrySet()) {
            final MetricId metricId = result.getKey();
            final MetricValueList values = result.getValue();

            if(values.isEmpty()) {
                // TODO: Do we really need to create a new MetricValueList here?
                ret.put(metricId, new MetricValueList(values.getStartTime(), values.getEndTime()));
                LOG.debug("No metrics found for: {}", metricId);
                continue;
            } else {
                // Adding the untouched metricId and values to the return list to avoid stripping the from the result.
                // It might make sense to never add the original values to the list and instead expect the use of the
                // copy operator, but for now this is probably more intuitive.
                ret.put(metricId, values);
            }

            for (final Entry<MetricId, MetricValueList> entrySet : correlationCandidates.entrySet()) {
                final MetricId candidateMetricId = entrySet.getKey();
                final MetricValueList candidateValues = entrySet.getValue();
                double correlated = 0.0;

                //
                // This is here because I'm not 100% sure I've caught all corner cases on shifted computation
                //
                try {
                    correlated = correlation.correlationWithTimeShift(values, candidateValues);
                } catch(final Exception e) {
                    LOG.warn("Would have thrown during shifted correlation computation: {}", e.getMessage());
                    // if we do throw, then just go with the normal correlation
                    correlated = correlation.correlation(values, candidateValues);
                }

                if (Math.abs(correlated) > 0.8) {
                    LOG.debug("Correlated {} with {}: {}", metricId, candidateMetricId, correlated);
                    ret.put(createNewId(candidateMetricId), candidateValues);
                }
            }
        }
        return ret;
    }

    /**
     * Given the context and a time range, obtain the metrics for every metric not already included in the context.
     * Note: this wasn't included in {@link MetricReader} for two reasons: there are currently no other uses for it and
     * it would be awkward to ask for all metrics for a given owner except this set we already have. This is going to
     * be memory expensive enough.
     * @param start the start time
     * @param end the end time
     * @param context the context
     * @return a map of candidate metric ids to their values for the given time range
     */
    private Map<MetricId, MetricValueList> getCorrelationCandidates(final long start,
                                                                    final long end,
                                                                    final Map<MetricId, MetricValueList> context) {
        final List<MetricRequest> correlationRequests = Lists.newArrayList();
        for (final MetricId metricId : metricMetadata.readMetricIds()) {
            // there's no point in retrieving the metric again
            if (context.keySet().contains(metricId)) {
                continue;
            }
            correlationRequests.add(new MetricRequest(metricId, start, end));
        }
        return metricReader.readMetrics(correlationRequests);
    }
}
