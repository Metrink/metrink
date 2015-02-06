package com.metrink.grammar.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;

/**
 * Averages values in a bucket of points and replaces those points with that average.
 */
public class BucketFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(BucketFunction.class);

    private static final double MAX_POINTS = 20.0;

    @Inject
    public BucketFunction() {
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(Map.Entry<MetricId, MetricValueList> result:context.entrySet()) {
            final MetricValueList values = result.getValue();

            if(values.size() <= MAX_POINTS) {
                continue;
            }

            LOG.debug("{} > {}", values.size(), MAX_POINTS);

            final int POINTS_PER_BUCKET = (int) FastMath.ceil(values.size() / MAX_POINTS);
            final MetricValueList newValues = new MetricValueList(values.getStartTime(), values.getEndTime());
            int curCount = 0;
            SummaryStatistics avgValue = null;
            boolean maxVal = true;

            for(MetricValue value:values) {
                if(curCount == 0) {
                    avgValue = new SummaryStatistics();
                }

                // add the value to our stats
                avgValue.addValue(value.getValue());

                // increment our count
                curCount++;

                // if we've gotten all of our values, insert it into our set
                if(curCount == POINTS_PER_BUCKET) {
                    if(maxVal) {
                        newValues.addMetricValue(new MetricValue(value.getTimestamp(), avgValue.getMax(), value.getUnits()));
                        maxVal = false;
                    } else {
                        newValues.addMetricValue(new MetricValue(value.getTimestamp(), avgValue.getMax(), value.getUnits()));
                        maxVal = true;
                    }
                    curCount = 0;
                }
            }

            LOG.debug("Replacing {} with {} for {}", new Object[] { values.size(), newValues.size(), result.getKey() });

            // replace the results in the map
            ret.put(createNewId(result.getKey()), newValues);
        }

        return ret;
    }
}
