package com.metrink.inject;

import org.joda.time.DateTime;

import com.google.inject.Provider;

/**
 * Concrete interface of a Provider for {@link DateTime}s.
 */
public interface DateTimeProvider extends Provider<DateTime> {

}
