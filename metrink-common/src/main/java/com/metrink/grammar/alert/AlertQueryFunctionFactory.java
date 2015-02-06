package com.metrink.grammar.alert;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.grammar.query.BaseQueryFunctionFactory;

/**
 * A pass-through concrete implementation of the BaseQueryFunctionFactory.
 */
public class AlertQueryFunctionFactory extends BaseQueryFunctionFactory {
    //private static final Logger LOG = LoggerFactory.getLogger(AlertQueryFunctionFactory.class);
    private static final long serialVersionUID = 794619398041345666L;

    @Inject
    public AlertQueryFunctionFactory(Injector injector) {
        super(injector);
    }
}
