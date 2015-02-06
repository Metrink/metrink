package com.metrink.grammar.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.utils.MilliSecondUtils;


public class DerivativeFunction extends QueryFunction {
    private static final Logger LOG = LoggerFactory.getLogger(DerivativeFunction.class);

    @Inject
    public DerivativeFunction() {
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
                final double valueDiff = values.get(i+1).getValue() - values.get(i).getValue();
                final double timeDiff = MilliSecondUtils.msToMinutes(values.get(i+1).getTimestamp() - values.get(i).getTimestamp());

                // add the value as rise/run
                newValues.add(new MetricValue(values.get(i)).setValue(valueDiff/timeDiff));
            }

            // replace the result
            ret.put(createNewId(result.getKey()), new MetricValueList(newValues));
        }

        return ret;
    }

}
