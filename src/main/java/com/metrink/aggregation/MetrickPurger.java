package com.metrink.aggregation;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.config.MetrinkCollectorSettings;
import com.metrink.inject.DateTimeProvider;
import com.metrink.metric.io.MetricReaderWriter;

public class MetrickPurger implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MetrickPurger.class);

    private static final long MS_TO_DAY = TimeUnit.DAYS.toMillis(1);

    private final int retentionDays;
    private final MetricReaderWriter metricReaderWriter;
    private final DateTimeProvider dateTimeProvider;

    @Inject
    public MetrickPurger(final MetricReaderWriter metricReaderWriter,
                         final DateTimeProvider dateTimeProvider,
                         final MetrinkCollectorSettings settings) {
        this.metricReaderWriter = metricReaderWriter;
        this.dateTimeProvider = dateTimeProvider;
        this.retentionDays = settings.getRetentionDays();
    }

    @Override
    public void run() {
        final long curTime = dateTimeProvider.get().getMillis();

        // we need this try - catch block so events
        // will continue to run even with exceptions
        try {
            // setup the time
            final long time = curTime - (MS_TO_DAY * retentionDays);

            // remove the metrics from the persistor
            final int purged = metricReaderWriter.deleteMetrics(time);

            LOG.debug("Purged {} metrics before {} (cur time {})",
                    new Object[]{ purged+"", time+"", curTime+""});
        } catch(final Exception e) {
            LOG.error("Caught top-level exception: {}", e.getMessage(), e);
        }
    }

}