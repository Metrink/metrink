package com.metrink.grammar.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.query.BaseQueryFunctionFactory;
import com.metrink.grammar.query.QueryFunction;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GraphQueryFunctionFactory extends BaseQueryFunctionFactory {
    private static final long serialVersionUID = 9181650035417087137L;

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GraphQueryFunctionFactory.class);

    private static final ImmutableMap<String, Class<? extends QueryFunction>> FUNCTION_MAP =
            new ImmutableMap.Builder<String, Class<? extends QueryFunction>>()
            .put("graph", RickshawFunction.class)
            .put("area",  AreaGraphFunction.class)
            .put("histo", HistogramFunction.class)
            .build();

    @Inject
    public GraphQueryFunctionFactory(final Injector injector) {
        super(injector);

        // add in these functions
        functionMap.putAll(FUNCTION_MAP);
    }

    // seems to be needed for deserialization
    public GraphQueryFunctionFactory() {
        super(null);
    }
}
