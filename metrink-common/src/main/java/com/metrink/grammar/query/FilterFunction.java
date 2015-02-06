package com.metrink.grammar.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.Argument;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.NumberArgument;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;

/**
 * Filters out metrics that are equal to the passed argument.
 */
public class FilterFunction extends QueryFunction {

    @Inject
    public FilterFunction() {
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        Collection<Double> arguments = null;

        try {
            arguments = Collections2.transform(this.getArgs(), new Function<Argument, Double>() {

                @Override
                public Double apply(Argument input) {

                    if(! (input instanceof NumberArgument)) {
                        throw new RuntimeException("Filter arguments must be numbers");
                    }

                    return ((NumberArgument) input).getDouble();
                }

            });
        } catch(RuntimeException e) {
            throw new MetrinkParseException(e);
        }

        final Set<Double> excludeSet = new HashSet<Double>(arguments);


        for(Map.Entry<MetricId, MetricValueList> result:context.entrySet()) {
            final MetricValueList valueList = result.getValue();

            if(valueList.isEmpty()) {
                continue;
            }

            final Double value = isSameValue(valueList);

            if(value == null || excludeSet.contains(value)) {
                continue;
            } else {
                // include this result
                ret.put(createNewId(result.getKey()), valueList);
            }
        }

        return ret;
    }

    /**
     * Checks to see if all the values are the same, if so, then returns that value, otherwise returns null.
     * Always returns null for NaN
     * @param values the values to check
     * @return the value if they're all the same, Null otherwise.
     */
    private Double isSameValue(MetricValueList values) {
        final List<MetricValue> valueList = values.getValues();

        if(valueList.isEmpty()) {
            return null;
        }

        // set our current value to the first one
        final Double initValue = Double.valueOf(valueList.get(0).getValue());

        for(MetricValue value:values) {
            Double curValue = Double.valueOf(value.getValue());

            if(curValue.equals(Double.NaN) ||
               curValue.equals(Double.NEGATIVE_INFINITY) ||
               curValue.equals(Double.POSITIVE_INFINITY)) {
                return null;
            }

            // if it's not the same, then return null
            if(!initValue.equals(curValue)) {
                return null;
            }
        }

        return initValue;
    }

}
