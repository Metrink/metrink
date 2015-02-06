package com.metrink.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.login.LoginPage;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

@Singleton
public class GuicePageFactory implements IPageFactory {
    public static final Logger LOG = LoggerFactory.getLogger(GuicePageFactory.class);

    private Injector injector;

    private final ConcurrentMap<String, Boolean> pageToBookmarkableCache = Generics.newConcurrentHashMap();

    @Inject
    public GuicePageFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public <C extends IRequestablePage> C newPage(final Class<C> pageClass) {
        LOG.debug("Creating new {} page without parameters", pageClass.getName());

        if (!Application.get().getSecuritySettings().getAuthorizationStrategy().isInstantiationAuthorized(pageClass)) {
            throw new RestartResponseAtInterceptPageException(LoginPage.class);
        }

        return injector.createChildInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(PageParameters.class).toInstance(new PageParameters());
            }

        }).getInstance(pageClass);
    }

    @Override
    public <C extends IRequestablePage> C newPage(final Class<C> pageClass, final PageParameters parameters) {
        LOG.debug("Creating new {} page with parameters: {}", pageClass.getName(), parameters);

        return injector.createChildInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(PageParameters.class).toInstance(parameters);
            }

        }).getInstance(pageClass);
    }

    @Override
    public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass) {
        Boolean result = pageToBookmarkableCache.get(pageClass.getName());

        if(result == null) {

            // go through all of the constructors looking for the @Assisted annotation
            for(final Constructor<?> constructor:pageClass.getConstructors()) {
                // if we find a constructor that is NOT marked with @Inject,
                // then we're doing it wrong!

                boolean foundInject = false;

                for(final Annotation annotation:constructor.getDeclaredAnnotations()) {
                    if(annotation.annotationType().equals(Inject.class)) {
                        foundInject = true;
                        break;
                    }
                }

                if(foundInject == false) {
                    result = false;
                    break;
                }

                // go through the annotations on the parameters
                for(final Annotation[] params:constructor.getParameterAnnotations()) {
                    for(final Annotation annotation:params) {
                        if(annotation.annotationType().equals(Assisted.class)) {
                            result = false;
                            break;
                        }
                    }
                }
            }

            // if result is still null by here,
            // then all the constructors are OK
            if(result == null) {
                result = true;
            }

            LOG.debug("Is {} bookmarkable? {}", pageClass.getName(), result);

            pageToBookmarkableCache.put(pageClass.getName(), result);
        }

        return result;
    }
}
