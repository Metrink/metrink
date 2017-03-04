package com.metrink.metric;

import java.io.Serializable;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Function;

/**
 * The value of a Metric.
 *
 * MetricValues are comparable via timestamp.
 */
public class MetricValue implements Serializable, Comparable<MetricValue> {

    private static final long serialVersionUID = 1L;
    private long timestamp; // all timestamps are number of ms since epoch, 1970-01-01T00:00:00Z
    private double value;
    private String units;


    public MetricValue() {

    }

    public MetricValue(final long timestamp, final double value, final String units) {
        this.timestamp = timestamp;
        this.value = value;
        this.units = units;
    }

    public MetricValue(final MetricValue metricValue) {
        this.timestamp = metricValue.timestamp;
        this.value = metricValue.value;
        this.units = metricValue.units;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();

        sb.append(timestamp);
        sb.append(" ");
        sb.append(value);

        if(units != null) {
            sb.append(units);
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

        final MetricValue rhs = (MetricValue) obj;

        return new EqualsBuilder()
                      .append(timestamp, rhs.getTimestamp())
                      .append(value, rhs.getValue())
                      .append(units, rhs.getUnits())
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(timestamp)
          .append(value)
          .append(units)
          .toHashCode();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MetricValue setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public double getValue() {
        return value;
    }

    public MetricValue setValue(double value) {
        this.value = value;
        return this;
    }

    public String getUnits() {
        return units;
    }

    public MetricValue setUnits(String units) {
        this.units = units;
        return this;
    }

    @Override
    public int compareTo(MetricValue metricValue) {
        return new CompareToBuilder().append(this.timestamp, metricValue.timestamp).toComparison();
    }

    /**
     * A {@link Function} that returns the timestamp from a {@link MetricValue}.
     */
    public static class GetTimestampFunction implements Function<MetricValue, Long> {
        @Override
        public Long apply(MetricValue input) {
            return input.getTimestamp();
        }
    }

    /**
     * A {@link Function} that returns the value from a {@link MetricValue}.
     */
    public static class GetValueFunction implements Function<MetricValue, Double> {
        @Override
        public Double apply(MetricValue input) {
            return input.getValue();
        }
    }

}
