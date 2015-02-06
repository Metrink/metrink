/**
 *
 */
package com.metrink.grammar.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.RelativeTimeArgument;
import com.metrink.grammar.query.QueryFunction;
import com.metrink.metric.DisplayMetricId;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.metric.io.MetricReader;

/**
 * A {@link QueryFunction} that reads metrics from a MetricReader.
 */
public class MetricFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(MetricFunction.class);

    private final MetricReader metricReader;
    private final MetricMetadata metricMetadata;
    private final MetricId id;
    private final List<RelativeTimeArgument> relativeTimes;

    @Inject
    public MetricFunction(final MetricReader metricReader,
                          final MetricMetadata metricMetadata,
                          @Assisted final MetricId id,
                          @Assisted final List<RelativeTimeArgument> relativeTimes)  throws MetrinkParseException {

        super(null, null);

        try {
            checkNotNull(id, "MetricId cannot be null");
            checkNotNull(relativeTimes, "Relative time arguments cannot be null");
        } catch(NullPointerException e) {
            throw new MetrinkParseException(e.getMessage());
        }

        this.metricReader = metricReader;
        this.metricMetadata = metricMetadata;
        this.id = id;
        this.relativeTimes = relativeTimes;

        super.setName("metric");
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();
        final List<MetricId> expandedIds = metricMetadata.expandMetricId(id);
        List<MetricRequest> requests = new ArrayList<>();

        // add in the original request
        for(MetricId id:expandedIds) {
            requests.add(new MetricRequest(id, start, end));
        }

        // put in all the results
        ret.putAll(metricReader.readMetrics(requests));

        // go through all the offsets
        for(MetricId id:expandedIds) {
            // add in all the relative requests
            for(RelativeTimeArgument timeArg:relativeTimes) {
                final long timeOffset = timeArg.getTimeInMs();

                LOG.debug("OFFSET: {}", timeOffset);

                // get the offset values
                final MetricValueList offsetValues = metricReader.readMetrics(id, start + timeOffset, end + timeOffset);
                final MetricId newId = new DisplayMetricId(id).appendDisplayName(" " + timeArg);

                // add the results after shifting the timestamps
                ret.put(newId, offsetValues.shiftTimestamp(timeOffset * -1));
            }
        }

        return ret;
    }

    public interface MetricFunctionFactory {
        public MetricFunction create(@Assisted MetricId id,
                                     @Assisted List<RelativeTimeArgument> relativeTimes) throws MetrinkParseException;
    }
}
