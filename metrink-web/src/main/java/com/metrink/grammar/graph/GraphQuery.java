package com.metrink.grammar.graph;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.Query;
import com.metrink.grammar.query.AverageFunction;
import com.metrink.grammar.query.ConnectorNode;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.grammar.query.QueryNode;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The parsed and "type checked" representation of a Graph Query.
 */
public class GraphQuery implements Query {
    private static final Logger LOG = LoggerFactory.getLogger(GraphQuery.class);

    private static final int MAX_POINTS_PER_METRIC = 2000;
    private static final int MAX_TOTAL_POINTS = 20000;

    private final QueryFunctionFactory functionFactory;
    private QueryNode rootNode;
    private final long start;
    private final long end;

    @Inject
    public GraphQuery(final QueryFunctionFactory functionFactory,
                      @Assisted QueryNode rootNode,
                      @Assisted("start") long start,
                      @Assisted("end") long end) {
        this.functionFactory = functionFactory;
        this.rootNode = rootNode;
        this.start = start;
        this.end = end;
    }

    public long getStartTime() {
        return start;
    }

    public long getEndTime() {
        return end;
    }

    /**
     * Executes the query returning a GraphObject
     * @return The {@link GraphObject} for the query.
     * @throws MetrinkParseException
     */
    public GraphObject execute() throws MetrinkParseException {

        // check to see if the last thing is a GraphFunction
        if(! (rootNode.getRightChild() instanceof GraphFunction) ) {
            rootNode = new ConnectorNode(rootNode, functionFactory.create("graph"), ConnectorNode.Type.COPY);
        }

        try {
            Map<MetricId, MetricValueList> result = rootNode.getLeftChild().process(start, end, ImmutableMap.<MetricId, MetricValueList>of());

            long numPoints = 0;
            boolean singleMetricOverMax = false;

            // count how many points we have, never go over MAX_POINTS points across all metrics
            for(MetricValueList valueList:result.values()) {
                if(valueList.size() > MAX_POINTS_PER_METRIC) {
                    singleMetricOverMax = true;
                }

                numPoints += valueList.size();
            }

            if(numPoints > MAX_TOTAL_POINTS || singleMetricOverMax) {
                // tell the user they asked for a lot of metrics
                org.apache.wicket.Session.get().warn("This query returned a lot of metrics. Values have been averaged to prevent your browser from locking up.");

                /*
                 * Should really change this to a moving average.
                 */

                // MAX_POINTS / result.size() = number of points per metric
                //final int pointsPerMetric = (int) (MAX_POINTS_PER_METRIC / result.size());
                final AverageFunction avgFun = new AverageFunction(MAX_POINTS_PER_METRIC);

                LOG.debug("NUM POINTS: {} POINTS PER METRIC: {}", numPoints, MAX_POINTS_PER_METRIC);

                // run the average across the results
                result = avgFun.process(start, end, ImmutableMap.copyOf(result));
            }

            // go through the metrics and add a single point at zero for those that don't have any values
            for(Map.Entry<MetricId, MetricValueList> entry:result.entrySet()) {
                final MetricValueList valueList = entry.getValue();

                if(valueList.size() == 0) {
                    // if we don't have any points, we add a bogus point at the start so it shows in the legend
                    valueList.addMetricValue(new MetricValue(valueList.getStartTime(), 0, ""));
                }
            }

            return ((GraphFunction) rootNode.getRightChild()).getGraphObject(result);
        } catch(NullPointerException|IllegalArgumentException e) {
            final String msg = e.getMessage(); // could be null

            LOG.error("Error processing GraphQuery: {}", msg, e);
            throw new MetrinkParseException(msg == null ? "Error processing graph query" : msg);
        }
    }

    /**
     * Copies the context creating ONLY new MetricIds in the process.
     * @param context the context to "deep" copy
     * @return a copy of the map with new {@link MetricId}s
     */
    public static Map<MetricId, MetricValueList> copyContext(final Map<MetricId, MetricValueList> context) {
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(final Map.Entry<MetricId, MetricValueList> entry:context.entrySet()) {
            ret.put(new MetricId(entry.getKey()), entry.getValue());
        }

        return ret;
    }

    public interface GraphQueryFactory {
        public GraphQuery create(QueryNode rootNode, @Assisted("start") long start, @Assisted("end") long end);
    }

}
