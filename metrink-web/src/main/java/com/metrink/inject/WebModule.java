package com.metrink.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.metrink.config.MetrinkSettings;
import com.metrink.grammar.graph.GraphQuery.GraphQueryFactory;
import com.metrink.grammar.graph.GraphQueryFunctionFactory;
import com.metrink.grammar.graph.MetricFunction.MetricFunctionFactory;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.MetrinkSession.MetrinkSessionFactory;
import com.metrink.gui.admin.user.UserDataProvider.UserDataProviderFactory;
import com.metrink.gui.graphing.QueryWizardPanel.QueryWizardPanelFactory;
import com.metrink.gui.search.MetricDataProvider.MetricDataProviderFactory;

public class WebModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(WebModule.class);

    private final MetrinkSettings settings;

    public WebModule(final MetrinkSettings settings) {
        LOG.debug("Constructing web guice module");
        this.settings = settings;
    }

    @Override
    protected void configure() {
        LOG.debug("Executing configure on web guice module");

        bind(QueryFunctionFactory.class).to(GraphQueryFunctionFactory.class);

        install(new FactoryModuleBuilder().build(GraphQueryFactory.class));
        install(new FactoryModuleBuilder().build(MetricDataProviderFactory.class));
        install(new FactoryModuleBuilder().build(MetricFunctionFactory.class));
        install(new FactoryModuleBuilder().build(MetrinkSessionFactory.class));
        install(new FactoryModuleBuilder().build(UserDataProviderFactory.class));
        install(new FactoryModuleBuilder().build(QueryWizardPanelFactory.class));
    }

    @Provides
    public MetrinkSession provideMetrinkSession() {
        final org.apache.wicket.Session session = org.apache.wicket.Session.get();

        return (MetrinkSession)session;
    }
}
