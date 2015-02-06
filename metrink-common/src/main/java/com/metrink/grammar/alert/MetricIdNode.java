package com.metrink.grammar.alert;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.query.AbstractQueryNode;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValueList;

/**
 * This class simply holds a {@link MetricId} in a query tree.
 */
public class MetricIdNode extends AbstractQueryNode {
    private static final Logger LOG = LoggerFactory.getLogger(MetricIdNode.class);

    private final MetricId id;

    public MetricIdNode(MetricId id) {
        super();

        this.id = id;
    }

    public MetricId getMetricId() {
        return id;
    }

    @Override
    public Map<MetricId, MetricValueList> process(long start, long end, ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        throw new MetrinkParseException("Process should never be called on a MetricIdNode");
    }
}
