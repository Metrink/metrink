package com.metrink.metric.io;

import java.util.List;
import java.util.Map;

import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValueList;

/**
 * Reads {@link Metric}s from a source.
 */
public interface MetricReader {

    /**
     * Initializes the reader once during startup.
     */
    public void init();

    /**
     * Shuts down the system reader during shutdown.
     */
    public void shutdown();

    /**
     * Reads the {@link Metric}s in the request returning a possibly empty map.
     * @param requests A list of requests to be read.
     * @return a map of results.
     */
    public Map<MetricId, MetricValueList> readMetrics(List<MetricRequest> requests);

    /**
     * Reads a single {@link MetricValueList} for a given owner and time range.
     * @param id the id of the metric.
     * @param start the start time.
     * @param end the end time.
     * @return the {@link MetricValueList} filled with the metrics.
     */
    public MetricValueList readMetrics(MetricId id, long start, long end);

}
