package com.metrink.grammar.graph;

import java.util.Map;

import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.query.QueryFunction;
import com.metrink.gui.graphing.GraphPanel;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValueList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


/**
 * A function that produces a {@link GraphObject}.
 */
public abstract class GraphFunction extends QueryFunction {

    public GraphFunction() {
        super(null, null);
    }

    @Override
    public final Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> searchResults) throws MetrinkParseException {
        // To avoid storing state within the GraphFunction, making processResults final with a no-op.
        return Maps.newHashMap();  //TODO: double check?!?
    }

    /**
     * Get a POJO containing all needed information to render the {@link GraphPanel}.
     * @param searchResults the search results to be rendered
     * @return the {@link GraphObject}
     * @throws MetrinkParseException if the graph cannot process the search results
     */
    public abstract GraphObject getGraphObject(final Map<MetricId, MetricValueList> searchResults)
            throws MetrinkParseException;
}
