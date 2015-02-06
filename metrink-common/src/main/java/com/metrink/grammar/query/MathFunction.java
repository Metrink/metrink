package com.metrink.grammar.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.metrink.grammar.Argument;
import com.metrink.grammar.BooleanArgument;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValue.GetTimestampFunction;
import com.metrink.metric.MetricValueList;

public class MathFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(MathFunction.class);

    private boolean useAvg = true;
    private final char op;

    /*
     * We don't have an @Inject here because this is actually constructed by "new" not the QueryFunctionFactory.
     */
    public MathFunction(final QueryNode leftChild, final QueryNode rightChild, final char op) {
        super(leftChild, rightChild);
        this.op = op;
        this.setArgs(ImmutableList.<Argument>of());
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> normalizedContext = combineContext(start, end, context);

        // normalize the lists
        final int LENGTH = MathFunction.normalizeMetrics(normalizedContext, useAvg);
        final List<MetricValue> computedValues = new ArrayList<>();
        final List<MetricValueList> normalizedValues = Lists.newArrayList(normalizedContext.values());

        // go through each value
        for(int cur = 0; cur < LENGTH; ++cur) {
            MetricValue curValue = null;

            // start the math operation with the cur value in the first list
            double res = normalizedValues.get(0).getValues().get(cur).getValue();

            // in each list
            for(int i=1; i < normalizedValues.size(); ++i) {
                curValue = normalizedValues.get(i).getValues().get(cur);

                switch(op) {
                case '+':
                    res += curValue.getValue();
                    break;
                case '-':
                    res -= curValue.getValue();
                    break;
                case '*':
                    res *= curValue.getValue();
                    break;
                case '/':
                    res /= curValue.getValue();
                    break;
                default:
                    throw new MetrinkParseException("Unknown math operation: " + op);
                }
            }

            computedValues.add(new MetricValue(curValue).setValue(res));
        }

        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        // combine the MetricIds and put them into ret with the values
        ret.put(combineMetricIds(normalizedContext.keySet(), op), new MetricValueList(computedValues));

        return ret;
    }

    /**
     * Combines a set of IDs, taking only the unique values
     * @param ids a set of {@link MetricId}s.
     * @param op the operation used to concatinate the names.
     * @return A combined {@link MetricId}
     */
    private MetricId combineMetricIds(final Set<MetricId> ids, final char op) {
        final MetricId first = ids.iterator().next();

        if(ids.size() == 1) {
            return first;
        }

        final Set<String> devices = new HashSet<>();
        final Set<String> groups = new HashSet<>();
        final Set<String> names = new HashSet<>();

        for(MetricId id:ids) {
            devices.add(id.getDevice());
            groups.add(id.getGroupName());
            names.add(id.getName());
        }

        final MetricId ret = new MetricId(StringUtils.join(devices, op),
                                          StringUtils.join(groups, op),
                                          StringUtils.join(names, op));

        return ret;
    }

    private Map<MetricId, MetricValueList> combineContext(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> combinedContext = new LinkedHashMap<>();

        // nothing in the context, so go with right & left children
        if(context.isEmpty()) {
            final Map<MetricId, MetricValueList> leftResult = leftChild.process(start, end, context);
            final Map<MetricId, MetricValueList> rightResult = rightChild.process(start, end, context);

            // make sure we only have one for each
            if(leftResult.size() != 1 || rightResult.size() != 1) {
                throw new MetrinkParseException("When performing math on metrics, each metric must only return one result");
            }

            // add left then right
            combinedContext.putAll(leftResult);
            combinedContext.putAll(rightResult);
        } else {
            if(context.size() > 2 && (op == '-' || op == '/')) {
                throw new MetrinkParseException("Cannot subtract or divide more than two metrics");
            } else if(context.size() == 1) {
                throw new MetrinkParseException("Trying to perform " + op + " on a single value");
            }

            // if the user passed in function(false)
            if(!this.getArgs().isEmpty() && this.getArgs().get(0).equals(BooleanArgument.False)) {
                useAvg = false;
            }

            //
            // FIXME: There is a bug here as the immutable map coming in might NOT be in the right order
            //
            combinedContext.putAll(context);
        }

        return combinedContext;
    }


    /**
     * Makes all of the lists the same length by adding values.
     * @param map the map with the lists.
     * @param useAvg if the average of two values should be used to add values, otherwise simply zero.
     * @return the length of the lists.
     */
    static int normalizeMetrics(final Map<MetricId, MetricValueList> map, final boolean useAvg) {
        final SortedSet<Long> times = new TreeSet<>();

        // go through all the metrics getting all the times
        for(final MetricValueList list:map.values()) {
            if(list.size() == 0) {
                continue;
            }

            times.addAll(Collections2.transform(list.getValues(), new GetTimestampFunction()));
        }

        final Iterator<Entry<MetricId, MetricValueList>> it = map.entrySet().iterator();

        // go through all the metrics normalizing their lists
        while(it.hasNext()) {
            final Entry<MetricId, MetricValueList> entry = it.next();

            // we just skip blank value lists
            if(entry.getValue().size() == 0) {
                it.remove();
            } else {
                entry.setValue(MetricValueList.fillInMissingValues(entry.getValue(), times, useAvg));
            }
        }

        return times.size();
    }
}
