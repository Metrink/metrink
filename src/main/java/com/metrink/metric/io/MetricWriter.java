package com.metrink.metric.io;

import java.util.List;

import com.metrink.metric.Metric;


/**
 * Writes {@link Metric}s to a source.
 */
public interface MetricWriter {
    /**
     * Initializes the reader once during startup.
     */
    public void init();

    /**
     * Shuts down the system reader during shutdown.
     */
    public void shutdown();

    /**
     * Writes the {@link Metric}s to the source.
     * @param metrics the {@link Metric}s to write.
     */
    public void writeMetrics(List<Metric> metrics);

    /**
     * Deletes all of the {@link Metric}s before a given time.
     * @param before the timestamp which to delete {@link Metric}s before.
     * @return the number of {@link Metric}s removed.
     */
    public int deleteMetrics(long before);

}
