package com.metrink.inject;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.metrink.grammar.alert.AlertQueryFunctionFactory;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.parser.MetrinkJsonParser;
import com.metrink.parser.Parser;
import com.sop4j.dbutils.QueryRunner;

public class CollectorGuiceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(CollectorGuiceModule.class);

    public CollectorGuiceModule() {
    }

    @Override
    protected void configure() {
        bind(Parser.class).to(MetrinkJsonParser.class);

        bind(QueryFunctionFactory.class).to(AlertQueryFunctionFactory.class);

        initializeVelocity();
    }

    @Provides @Inject
    public QueryRunner providesQueryRunner(final DataSource dataSource) {
        return new QueryRunner(dataSource);
    }

    /**
     * Initialize Velocity. I'm not really certain this is the best location to initialize Velocity. I'm doing it here
     * because the email templates are initialized in this module.
     */
    private void initializeVelocity() {
        // We want to load the templates from the resources directory. The default is the CWD of tho java process.
        final ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.addProperty("resource.loader", "class");
        extendedProperties.addProperty("class.resource.loader.class",
                        org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
        Velocity.setExtendedProperties(extendedProperties);

        Velocity.init();
    }

    /**
     * Helper rethrow the Velocity exception. Catching Exception, because that's what velocity's sample does.
     * @param e the exception
     * @return nothing, this method always throws
     */
    private Template rethrowVelocityException(final Exception e) {
        LOG.error("Failed to load email template: {}", e.getMessage(), e);
        throw new IllegalStateException("Failed to load email template", e);
    }

    /**
     * Get the plain text email template.
     * @return the plain text email template
     */
    @Provides
    @Named("email-template-plain-text")
    protected Template getEmailTemplatePlainText() {
        try {
            return Velocity.getTemplate("templates/EmailAlert.txt.vm");
        } catch( final Exception e ) {
            return rethrowVelocityException(e);
        }
    }

    /**
     * Get the html email template. Note that we return the .min version.
     * @return the html email template
     */
    @Provides
    @Named("email-template-html")
    protected Template getEmailTemplateHtml() {
        try {
            return Velocity.getTemplate("templates/EmailAlert.min.html.vm");
        } catch( final Exception e ) {
            return rethrowVelocityException(e);
        }
    }
}
