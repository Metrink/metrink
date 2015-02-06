package com.metrink.grammar.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.Argument;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.NumberArgument;
import com.metrink.grammar.RelativeTimeArgument;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.utils.AggregateFunction;

public class AverageFunction extends QueryFunction {

    public static final Logger LOG = LoggerFactory.getLogger(AverageFunction.class);

    private static final AggregateFunction<MetricValue> fun = new AggregateFunction<MetricValue>() {

        @Override
        public MetricValue aggregate(Collection<? extends MetricValue> values) {
            final SummaryStatistics stats = new SummaryStatistics();

            for(MetricValue value:values) {
                stats.addValue(value.getValue());
            }

            // use the first value in the collection as the representative value
            return values.iterator().next().setValue(stats.getMean());
        }

    };

    private int totalPoints = -1;

    @Inject
    public AverageFunction() {
    }

    /**
     * Constructor for internal use.
     * @param arg the number of points.
     */
    public AverageFunction(final int totalPoints) {
        this.totalPoints = totalPoints;
        this.setArgs(ImmutableList.<Argument>of());
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();
        final int numArgs = getArgs().size();

        if(numArgs > 1) {
            throw new MetrinkParseException("Too many arguments for average function");
        }

        for(final Map.Entry<MetricId, MetricValueList> result:context.entrySet()) {
            final MetricValueList values = result.getValue();

            if(values.isEmpty()) {
                LOG.debug("No metrics found for: {}", result.getKey());

                // add the new results
                ret.put(createNewId(result.getKey()), new MetricValueList(values.getStartTime(), values.getEndTime()));
                continue;
            }

            List<MetricValue> aggregatedValues = null;

            if(totalPoints != -1) {
                aggregatedValues = values.aggregateByValuesInResult(totalPoints, fun);

                // make a copy for the ending value
                final MetricValue value = new MetricValue(aggregatedValues.get(0));
                value.setTimestamp(values.getLast().getTimestamp());

                // add the ending value to our list
                aggregatedValues.add(value);
            } else if(numArgs == 0) {
                aggregatedValues = values.aggregateByValuesInAggregation(values.size(), fun);

                // make a copy for the ending value
                final MetricValue value = new MetricValue(aggregatedValues.get(0));
                value.setTimestamp(values.getLast().getTimestamp());

                // add the ending value to our list
                aggregatedValues.add(value);
            } else if(numArgs == 1) {
                final Argument arg = getArgs().get(0);

                if(arg instanceof NumberArgument) {
                    aggregatedValues = values.aggregateByValuesInAggregation(((NumberArgument) arg).getInt(), fun);
                } else if(arg instanceof RelativeTimeArgument) {
                    aggregatedValues = values.aggregateByTime(((RelativeTimeArgument) arg).getDuration(),
                                                              ((RelativeTimeArgument) arg).getTimeUnit(),
                                                              fun);
                } else {
                    throw new MetrinkParseException("Unknown argument passed to average");
                }
            }

            final MetricValueList newValues = new MetricValueList(aggregatedValues);

            LOG.debug("Replacing {} with {} for {}", values.size(), newValues.size(), result.getKey());

            // add to the new results
            ret.put(createNewId(result.getKey()), newValues);
        }

        return ret;
    }

}
