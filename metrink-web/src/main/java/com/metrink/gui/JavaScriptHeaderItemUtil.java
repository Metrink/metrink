package com.metrink.gui;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.request.resource.JavaScriptPackageResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptHeaderItemUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptHeaderItemUtil.class);

    public static String forScript(final Class<?> scope, final String name) {
        final PackageResource resource = new JavaScriptPackageResource(scope, name, null, null, null);

        try {
             return IOUtils.toString(resource.getResourceStream().getInputStream());

        } catch (final ResourceStreamNotFoundException e) {
            LOG.error("Could not find javascript file: {}", name, e);
        } catch (final IOException e) {
            LOG.error("IOException while loading javascript: {}", name, e);
        }

        throw new IllegalStateException("Couldn't obtain script");
    }
}
