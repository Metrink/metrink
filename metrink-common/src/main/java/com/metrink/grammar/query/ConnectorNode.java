package com.metrink.grammar.query;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableMap;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValueList;


public class ConnectorNode extends AbstractQueryNode {

    public enum Type {
        PIPE,
        COPY,
        TERMINATOR
    }

    private final Type type;

    public ConnectorNode(final QueryNode leftChild, final QueryNode rightChild, final String connector) throws MetrinkParseException {
        super(leftChild, rightChild);

        switch(connector) {
        case "|":
            type = Type.PIPE;
            break;

        case ">|":
            type = Type.COPY;
            break;

        case "&":
            type = Type.TERMINATOR;
            break;

        default:
            throw new MetrinkParseException("Unknown connector: " + connector);
        }
    }

    public ConnectorNode(final QueryNode leftChild, final QueryNode rightChild, final Type type) throws MetrinkParseException {
        super(leftChild, rightChild);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final ConnectorNode rhs = (ConnectorNode) obj;

        return new EqualsBuilder()
                      .append(type, rhs.type)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(type)
          .toHashCode();
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        final Map<MetricId, MetricValueList> left  = leftChild.process(start, end, ImmutableMap.<MetricId, MetricValueList>of());
        final Map<MetricId, MetricValueList> right = rightChild.process(start, end, ImmutableMap.copyOf(left));

        // set the return to what's in the right
        Map<MetricId, MetricValueList> ret = right;

        // if we're copying, then add in what's on the left as well
        if(type.equals(Type.COPY)) {
            ret.putAll(left);
        }

        return ret;
    }
}
