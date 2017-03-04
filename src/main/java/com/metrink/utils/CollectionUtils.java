package com.metrink.utils;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

public class CollectionUtils {
    //private static final Logger LOG = LoggerFactory.getLogger(CollectionUtils.class);

    private CollectionUtils() {
    }

    /**
     * Takes an iterator and returns an iterable that can be iterated once.
     * @param source the iterator.
     * @return an iterable.
     * @see https://code.google.com/p/guava-libraries/issues/detail?id=796
     */
    public static <T> Iterable<T> once(final Iterator<T> source) {
        return new Iterable<T>() {
            private AtomicBoolean exhausted = new AtomicBoolean();

            @Override
            public Iterator<T> iterator() {
                Preconditions.checkState(!exhausted.getAndSet(true));
                return source;
            }
        };
    }

}
