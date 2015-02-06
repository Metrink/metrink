package com.metrink.metric;

/**
 * A request to read a set of metrics.
 */
public class MetricRequest {

    private final MetricId id;
    private final long start;
    private final long end;

    public MetricRequest(MetricId id, long start, long end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(start);
        sb.append(" -> ");
        sb.append(end);
        sb.append(": ");
        sb.append(id.toString());

        return sb.toString();
    }

    public MetricId getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
