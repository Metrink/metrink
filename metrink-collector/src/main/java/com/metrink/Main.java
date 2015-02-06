package com.metrink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.config.MetrinkCollectorSettings;
import com.metrink.config.MetrinkSettings;
import com.metrink.croquet.CroquetRest;
import com.metrink.croquet.CroquetRestBuilder;
import com.metrink.inject.CommonModule;

/**
 * Class that holds the main method for the collector.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final CroquetRest<MetrinkCollectorSettings> croquetRest = createBuilder(args).build();
        final MetrinkSettings settings = croquetRest.getSettings();

        croquetRest.addGuiceModule(new CommonModule(settings));

        croquetRest.run();
    }

    public static CroquetRestBuilder<MetrinkCollectorSettings> createBuilder(final String[] args) {
        final CroquetRestBuilder<MetrinkCollectorSettings> ret =
                CroquetRestBuilder.create(MetrinkCollectorSettings.class, args)
                                  .addProviderPackage("com.metrink.api");

        return ret;
    }
}
