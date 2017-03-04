package com.metrink.metric.io;

import java.util.List;
import java.util.Set;

import com.metrink.metric.MetricId;

/**
 * Interface defining access to Metric metadata, i.e., devices, groups, and owners.
 */
public interface MetricMetadata {

    /**
     * Initializes the reader once during startup.
     */
    public void init();

    /**
     * Shuts down the system reader during shutdown.
     */
    public void shutdown();

    /**
     * Reads all of the {@link MetricId}s.
     * @return the list of MetricIds.
     */
    public List<MetricId> readMetricIds();

    /**
     * Reads the list of the unique devices.
     * @return the list of unique devices.
     */
    public List<String> readUniqueDevices();

    /**
     * Reads the list of the unique groups.
     * @return the list of unique groups.
     */
    public List<String> readUniqueGroups();

    /**
     * Reads the list of the unique metric names.
     * @return the list of unique metric names.
     */
    public List<String> readUniqueMetricNames(final String group);

    /**
     * Given a {@link MetricId} expands it into a list of {@link MetricId}s.
     * @param id the {@link MetricId} to expand.
     * @return the expanded {@link MetricId}s.
     */
    public List<MetricId> expandMetricId(final MetricId id);

    /**
     * Reads the unique metrics.
     * @return a list of {@link MetricId}s.
     */
    public List<MetricId> readUniqueMetrics();

    /**
     * Persists the metric device, group, and name.
     * @param metricIds list of metric ids to persist
     * @return true if successful
     */
    public boolean writeMetricIds(final Set<MetricId> metricIds);

    public int deleteMetricId(final MetricId metricId);

}
