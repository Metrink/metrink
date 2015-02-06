package com.metrink.grammar.query;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.utils.MilliSecondUtils;

/**
 * A constant value for a fake metric.
 *
 * This is used during some math operation with a real metric.
 */
public class ConstantMetricNode extends AbstractQueryNode {

    public static final Logger LOG = LoggerFactory.getLogger(ConstantMetricNode.class);
    public static int suffix = 0; // we need a unique name for the metrics

    private final Double value;

    public ConstantMetricNode(final Double value) {
        super(null, null);
        this.value = value;
    }

    public ConstantMetricNode(final Long value) {
        this(new Double(value));
    }
    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final MetricId id = new MetricId("", "CONSTANT" + suffix++, "");
        final MetricValueList values = new MetricValueList(start, end);

        for(long time=MilliSecondUtils.msToMinutes(start); time < MilliSecondUtils.msToMinutes(end); ++time) {
            values.addMetricValue(new MetricValue(MilliSecondUtils.minutesToMs(time), value, ""));
        }

        return ImmutableMap.of(id, values);
    }
}
