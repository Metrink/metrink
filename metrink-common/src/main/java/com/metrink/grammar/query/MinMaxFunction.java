package com.metrink.grammar.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;

/**
 * A search function that draws a single line at either the max or min value.
 */
public class MinMaxFunction extends QueryFunction {

    @Inject
    public MinMaxFunction() {
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(Map.Entry<MetricId, MetricValueList> result:context.entrySet()) {
            final MetricValueList valueList = result.getValue();

            if(valueList.isEmpty()) {
                continue;
            }

            final SummaryStatistics stats = new SummaryStatistics();

            for(MetricValue value:valueList) {
                stats.addValue(value.getValue());
            }

            final MetricValueList newValues = new MetricValueList(valueList.getStartTime(), valueList.getEndTime());
            final MetricValue first = valueList.getFirst();
            final MetricValue last = valueList.getLast();

            switch(getName()) {
            case "min":
                newValues.addMetricValue(new MetricValue(first.getTimestamp(), stats.getMin(), first.getUnits()));
                newValues.addMetricValue(new MetricValue(last.getTimestamp(), stats.getMin(), last.getUnits()));
                break;

            case "max":
                newValues.addMetricValue(new MetricValue(first.getTimestamp(), stats.getMax(), first.getUnits()));
                newValues.addMetricValue(new MetricValue(last.getTimestamp(), stats.getMax(), last.getUnits()));
                break;

            default:
                throw new MetrinkParseException("Unknown function " + getName());
            }

            // replace the results in the map
            ret.put(createNewId(result.getKey()), newValues);
        }

        return ret;
    }

}
