package com.metrink.grammar.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.utils.MilliSecondUtils;


public class IntegralFunction extends QueryFunction {
    private static final Logger LOG = LoggerFactory.getLogger(IntegralFunction.class);

    @Inject
    public IntegralFunction() {
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(Map.Entry<MetricId, MetricValueList> result:context.entrySet()) {
            final List<MetricValue> values = result.getValue().getValues();

            if(values.isEmpty()) {
                LOG.debug("No metrics found for: {}", result.getKey());

                // add the new results
                ret.put(createNewId(result.getKey()), new MetricValueList(result.getValue().getStartTime(), result.getValue().getEndTime()));
                continue;
            }

            final List<MetricValue> newValues = new ArrayList<>(values.size());

            for(int i=0; i < values.size()-1; ++i) {
                final double iVal = values.get(i).getValue();
                final double iOneVal = values.get(i+1).getValue();
                final double timeDiff = MilliSecondUtils.msToMinutes(values.get(i+1).getTimestamp() - values.get(i).getTimestamp());
                final double square = timeDiff * FastMath.min(iVal, iOneVal);
                final double triangle = timeDiff * FastMath.abs(iVal - iOneVal) / 2;

                // add the value as the area of the square plus the area of the triangle
                newValues.add(new MetricValue(values.get(i)).setValue(square + triangle));
            }

            // replace the result
            ret.put(createNewId(result.getKey()), new MetricValueList(newValues));
        }

        return ret;
    }

}
