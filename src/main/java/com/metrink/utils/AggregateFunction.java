package com.metrink.utils;

import java.util.Collection;

/**
 * A function that operates over a {@link Collection} of values returning a single value.
 */
public interface AggregateFunction<T> {

    public T aggregate(final Collection<? extends T> values);
}
