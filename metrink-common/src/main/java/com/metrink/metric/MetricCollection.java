package com.metrink.metric;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetricCollection {

    @JsonProperty("d")
    private String device;

    @JsonProperty("m")
    private Collection<JsonMetric> jsonMetrics;

    /**
     * Get device.
     * @return the device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Set device.
     * @param device the device to set
     */
    public void setDevice(final String device) {
        this.device = device;
    }

    /**
     * Get metrics.
     * @return the metrics
     */
    public Collection<JsonMetric> getMetrics() {
        return jsonMetrics;
    }

    public void addMetric(JsonMetric jsonMetric) {
        jsonMetrics.add(jsonMetric);
    }

    /**
     * Set metrics.
     * @param jsonMetrics the metrics to set
     */
    public void setMetrics(final Collection<JsonMetric> jsonMetrics) {
        this.jsonMetrics = jsonMetrics;
    }

}
