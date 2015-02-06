package com.metrink.utils;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.wicket.injection.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for de-serializing Wicket objects.
 */
public class DeserializationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DeserializationUtils.class);

    /**
     * De-serialize a Wicket object repopulating transient fields annotated with {@link com.google.inject.Inject} using
     * Guice.
     * @param in the object input stream provided by obj#readObject
     * @param obj the object to inject upon
     * @throws IOException upon io exception
     * @throws ClassNotFoundException upon class exception
     */
    public static void readObject(final ObjectInputStream in, final Object obj)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        LOG.debug("Deserializing {}", obj.getClass().getName());

        final Injector injector = Injector.get();

        if(injector != null) {
            injector.inject(obj);
        }
    }
}
