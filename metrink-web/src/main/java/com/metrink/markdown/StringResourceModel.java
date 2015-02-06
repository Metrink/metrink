package com.metrink.markdown;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.io.Resources;

/**
 * Model representing the string contents of a resource.
 */
public class StringResourceModel extends LoadableDetachableModel<String> {
    private static final long serialVersionUID = 1L;

    private final Class<?> parent;
    private final String path;

    /**
     * Initialize the model using a parent class and relative path.
     * @param parent the parent class which will act as the resource root
     * @param path the relative path to append to said parent root
     */
    public StringResourceModel(final Class<?> parent, final String path) {
        this.parent = parent;
        this.path = path;
    }

    @Override
    public String load() {
        try {
            return Resources.toString(Resources.getResource(parent, path), Charset.forName("utf-8"));

        } catch (final IOException e) {
            throw new WicketRuntimeException("Unable to load " + path + " relative to " + parent.getName(), e);
        }
    }
}
