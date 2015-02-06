package com.metrink.metric;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a metric.
 */
public class JsonMetric {

    @JsonProperty("t")
    private String timestamp;

    @JsonProperty("g")
    private String group;

    @JsonProperty("n")
    private String name;

    @JsonProperty("v")
    private String value;

    public static JsonMetric of(String group, String name, double value, String timestamp) {
        final JsonMetric jsonMetric = new JsonMetric();

        jsonMetric.setGroup(group);
        jsonMetric.setName(name);
        jsonMetric.setTimestamp(timestamp);
        jsonMetric.setValue(String.valueOf(value));

        return jsonMetric;
    }

    /**
     * Get timestamp.
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp.
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get group.
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set group.
     * @param group the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * Get name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name.
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value the value to set
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
