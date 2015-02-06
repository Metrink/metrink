package com.metrink.grammar.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around {@link MathFunction} so multiplication can be used via a pipe.
 */
public class MultiplyFunction extends MathFunction {

    public static final Logger LOG = LoggerFactory.getLogger(MultiplyFunction.class);

    public MultiplyFunction() {
        super(null, null, '*');
    }
}
