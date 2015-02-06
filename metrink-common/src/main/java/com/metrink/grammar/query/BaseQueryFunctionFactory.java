package com.metrink.grammar.query;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.grammar.Argument;
import com.metrink.grammar.MetrinkParseException;

/**
 * Provides the base functions common between graphing and alerting.
 */
public abstract class BaseQueryFunctionFactory implements QueryFunctionFactory {
    private static final long serialVersionUID = -7276974215829523507L;

    public static final Logger LOG = LoggerFactory.getLogger(BaseQueryFunctionFactory.class);

    private static final ImmutableMap<String, Class<? extends QueryFunction>> BASE_FUNCTION_MAP =
            new ImmutableMap.Builder<String, Class<? extends QueryFunction>>()
            .put("avg", AverageFunction.class)
            .put("bucket", BucketFunction.class)
            .put("filter", FilterFunction.class)
            .put("deriv", DerivativeFunction.class)
            .put("int", IntegralFunction.class)
            .put("min", MinMaxFunction.class)
            .put("max", MinMaxFunction.class)
            .put("mavg", MovingAverageFunction.class)
            .put("mul", MultiplyFunction.class)
            .put("sum", AddFunction.class)
            .put("corr", CorrelationFunction.class)
            //.put("predict", PredictFunction.class) re-add once logic has been fixed
            .build();

    protected final Map<String, Class<? extends QueryFunction>> functionMap;

    private transient final Injector injector;

    @Inject
    public BaseQueryFunctionFactory(final Injector injector) {
        this.injector = injector;
        functionMap = Maps.newHashMap(BASE_FUNCTION_MAP);
    }

    @Override
    public QueryFunction create(final String name) throws MetrinkParseException {
        return create(name, ImmutableList.<Argument>of());
    }

    @Override
    public QueryFunction create(final String name, final List<Argument> args) throws MetrinkParseException {
        LOG.debug("Creating QueryFunction {} with {} args", name, args.size());
        final Class<? extends QueryFunction> clazz = functionMap.get(name);

        if(clazz == null) {
            throw new MetrinkParseException(name + " is not a valid function name");
        }

        return injector.getInstance(clazz).setName(name).setArgs(args);
    }

    @Override
    public List<String> getFunctions() {
        return  ImmutableList.copyOf(Ordering.natural().sortedCopy(functionMap.keySet()));
    }
}
