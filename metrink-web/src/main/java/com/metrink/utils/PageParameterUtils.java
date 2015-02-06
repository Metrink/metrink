package com.metrink.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageParameterUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PageParameterUtils.class);

    private PageParameterUtils() {
    }

    /**
     * Given a bean, constructs a {@link PageParameters} object containing all the properties.
     * @param bean the bean to inspect.
     * @return a {@link PageParameters} object with all the beans values set.
     */
    public static PageParameters beanToParams(Object bean) {
        final PageParameters params = new PageParameters();

        if(bean == null) {
            return params;
        }

        try {
            Map<String, String> properties = BeanUtils.describe(bean);

            for(Map.Entry<String, String> property:properties.entrySet()) {
                // cannot have null values
                if(property.getValue() == null) {
                    continue;
                }

                LOG.trace("Adding param: {} -> {}", property.getKey(), property.getValue());
                params.add(property.getKey(), property.getValue());
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("Error getting properties: {}", e.getMessage());
        }

        return params;
    }

    /**
     * Constructs a bean object from page parameters.
     * @param beanClass the class of the bean.
     * @param params the page parameters containing the properties.
     * @return a newly constructed object with the properties set or null if the object couldn't be constructed.
     */
    public static <T> T paramsToBean(Class<T> beanClass, PageParameters params) {
        T ret = null;

        // class is null, return a null object
        if(beanClass == null) {
            return ret;
        }

        try {
            ret = beanClass.getConstructor().newInstance();
        } catch (InstantiationException |
                 IllegalAccessException |
                 IllegalArgumentException |
                 InvocationTargetException |
                 NoSuchMethodException |
                 SecurityException e) {
            LOG.error("Error constructing object {}: {}", beanClass.getCanonicalName(), e.getMessage());
        }

        // no params, return an empty object
        if(params == null) {
            return ret;
        }

        for(NamedPair pair:params.getAllNamed()) {
            try {
                LOG.trace("Setting property: {} -> {}", pair.getKey(), pair.getValue());
                BeanUtils.setProperty(ret, pair.getKey(), pair.getValue());
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Error setting property {}: {}", pair.getKey(), e.getMessage());
            }
        }

        return ret;
    }
}
