package com.metrink.metric.io;

import com.google.inject.Inject;
import com.metrink.croquet.modules.ManagedModule;

public class ManagedMetricReaderWriter implements ManagedModule {
    private final MetricReaderWriter metricReaderWriter;

    @Inject
    public ManagedMetricReaderWriter(final MetricReaderWriter metricReaderWriter) {
        this.metricReaderWriter = metricReaderWriter;
    }

    @Override
    public void start() {
        metricReaderWriter.init();
    }

    @Override
    public void stop() {
        metricReaderWriter.shutdown();
    }
}
