package com.metrink.grammar.query;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.metrink.grammar.Argument;
import com.metrink.metric.DisplayMetricId;
import com.metrink.metric.MetricId;

/**
 * A function found in a query.
 */
public abstract class QueryFunction extends AbstractQueryNode {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(QueryFunction.class);

    private String name;
    private ImmutableList<Argument> args;

    /**
     * Used by functions which won't have children.
     */
    public QueryFunction() {
    }

    /**
     * Constructor used by math operations as they'll have children.
     * @param leftChild the left child in the tree.
     * @param rightChild the right child in the tree.
     */
    public QueryFunction(final QueryNode leftChild, final QueryNode rightChild) {
        super(leftChild, rightChild);
    }

    public String getName() {
        return name;
    }

    protected QueryFunction setName(final String name) {
        this.name = name;
        return this;
    }

    public ImmutableList<Argument> getArgs() {
        return args;
    }

    QueryFunction setArgs(final List<Argument> args) {
        this.args = ImmutableList.copyOf(args);
        return this;
    }

    /**
     * Transform a MetricId's name to include the function name.
     * @param id the metric id
     * @return the transformed metric id
     */
    public MetricId createNewId(final MetricId id) {
        final DisplayMetricId ret = new DisplayMetricId(id);

        if(getName() != null) {
            return ret.appendDisplayName(" " + getName());
        } else {
            return ret;
        }
    }
}
