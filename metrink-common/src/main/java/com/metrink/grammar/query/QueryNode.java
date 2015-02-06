package com.metrink.grammar.query;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValueList;

/**
 * Interface for all nodes in the query tree.
 */
public interface QueryNode {

    /**
     * Returns left child in the tree.
     *
     * Should be processed first.
     * @return left child in the tree.
     */
    public QueryNode getLeftChild();

    /**
     * Returns right child in the tree.
     *
     * Should be processed second.
     * @return right child in the tree.
     */
    public QueryNode getRightChild();

    /**
     * Given the map of {@link MetricId}s and {@link MetricValueList}s, produce a new map.
     *
     * Note: the context could be empty.
     *
     * @param start the start time of the query.
     * @param end the end time of the query.
     * @param context the context of any previous calls.
     * @return the modified context after processing this node.
     */
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException;
}
