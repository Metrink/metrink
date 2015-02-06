package com.metrink.grammar.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around {@link MathFunction} so addition can be used via a pipe.
 */
public class AddFunction extends MathFunction {
    public static final Logger LOG = LoggerFactory.getLogger(AddFunction.class);

    public AddFunction() {
        super(null, null, '+');
    }

}
