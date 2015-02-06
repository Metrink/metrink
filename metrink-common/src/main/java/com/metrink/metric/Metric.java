package com.metrink.metric;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Metric implements Serializable {

    private static final long serialVersionUID = 1L;

    private MetricId id;
    private MetricValue value;

    /**
     * Default constructor utilized by DBUtils when performing SELECTs.
     */
    public Metric() {
        id = new MetricId();
        value = new MetricValue();
    }

    public Metric(final MetricId id,
                  final long timestamp,
                  final double value,
                  final String units) {
        this(id, new MetricValue(timestamp, value, units));
    }

    public Metric(final String device,
                  final String group,
                  final String name,
                  final long timestamp,
                  final double value,
                  final String units) {
        this(new MetricId(device, group, name), new MetricValue(timestamp, value, units));
    }

    public Metric(final MetricId id, final MetricValue value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(value.getTimestamp() + "\t");

        sb.append(id.toString());
        sb.append(" ");
        sb.append(value.getValue());

        if(value.getUnits() != null) {
            sb.append(value.getUnits());
        }

        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final Metric rhs = (Metric) obj;

        return new EqualsBuilder()
                      .append(id, rhs.id)
                      .append(value, rhs.getMetricValue())
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(id)
          .append(value)
          .toHashCode();
    }

    public MetricId getId() {
        return id;
    }

    public Metric setId(final MetricId id) {
        this.id = id;
        return this;
    }

    public MetricValue getMetricValue() {
        return value;
    }

    public void setMetricValue(final MetricValue value) {
        this.value = value;
    }

    public String getDevice() {
        return id.getDevice();
    }

    public void setDevice(final String device) {
        id.setDevice(device);
    }

    public String getGroupName() {
        return id.getGroupName();
    }

    public void setGroupName(final String groupName) {
        id.setGroupName(groupName);
    }

    public String getName() {
        return id.getName();
    }

    public void setName(final String name) {
        id.setName(name);
    }

    public long getTimestamp() {
        return value.getTimestamp();
    }

    public void setTimestamp(final long timestamp) {
        value.setTimestamp(timestamp);
    }

    public double getValue() {
        return value.getValue();
    }

    public void setValue(final double value) {
        this.value.setValue(value);
    }

    public String getUnits() {
        return value.getUnits();
    }

    public void setUnits(final String units) {
        value.setUnits(units);
    }
}
