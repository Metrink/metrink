package com.metrink;

import com.metrink.config.MetrinkSettings;
import com.metrink.config.MetrinkWebSettings;
import com.metrink.croquet.CroquetWicket;
import com.metrink.croquet.CroquetWicketBuilder;
import com.metrink.gui.Application;
import com.metrink.gui.ExceptionPage;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.login.LoginPage;
import com.metrink.inject.CommonModule;
import com.metrink.inject.WebModule;
import com.metrink.metric.io.ManagedMetricReaderWriter;

/**
 * Launch Metrink Web.
 */
public class Main {

    /**
     * Launch Metrink Web.
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final CroquetWicket<MetrinkWebSettings> croquet = createBuilder(args).build();
        final MetrinkSettings settings = croquet.getSettings();

        croquet.addGuiceModule(new CommonModule(settings));
        croquet.addGuiceModule(new WebModule(settings));


        croquet.addManagedModule(ManagedMetricReaderWriter.class);

        croquet.run();
    }

    public static CroquetWicketBuilder<MetrinkWebSettings> createBuilder(final String[] args) {
        final CroquetWicketBuilder<MetrinkWebSettings> ret =
                CroquetWicketBuilder.create(MetrinkWebSettings.class, args)
                             .setHomePageClass(LoginPage.class)
                             .setLoginPageClass(LoginPage.class)
                             .setWebApplicationClass(Application.class)
                             .setWicketSessionClass(MetrinkSession.class)
                             .setExceptionPageClass(ExceptionPage.class)
                             .addDbProperty("zeroDateTimeBehavior", "convertToNull");

        return ret;
    }

}
