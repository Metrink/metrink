package com.metrink.grammar.query;

import java.io.Serializable;
import java.util.List;

import com.metrink.grammar.Argument;
import com.metrink.grammar.MetrinkParseException;

/**
 * An interface to a factory that creates {@link QueryFunction}s.
 */
public interface QueryFunctionFactory extends Serializable {

    /**
     * Creates an instance of the {@link QueryFunction} by name.
     * @param name the name of the {@link QueryFunction}.
     * @return a new instance of the {@link QueryFunction}.
     * @throws {@link MetrinkParseException} if the function isn't found.
     */
    public QueryFunction create(final String name) throws MetrinkParseException;

    /**
     * Creates an instance of the {@link QueryFunction} by name adding the arguments.
     * @param name the name of the {@link QueryFunction}.
     * @param args the arguments passed to the function.
     * @return a new instance of the {@link QueryFunction}.
     * @throws {@link MetrinkParseException} if the function isn't found.
     */
    public QueryFunction create(final String name, final List<Argument> args) throws MetrinkParseException;


    /**
     * Returns a list of the functions provided by this factory.
     * @return the list of functions provided by this factory.
     */
    public List<String> getFunctions();

}
