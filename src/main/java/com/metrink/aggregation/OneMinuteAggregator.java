package com.metrink.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.metrink.inject.DateTimeProvider;
import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.io.MetricWriter;

@Singleton
public class OneMinuteAggregator implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(OneMinuteAggregator.class);
    private static final long MS_TO_MIN = TimeUnit.MINUTES.toMillis(1);

    private final MetricWriter metricWriter;
    private final DateTimeProvider dateTimeProvider;
    private final ConcurrentLinkedQueue<Metric> metricsQueue = new ConcurrentLinkedQueue<Metric>();

    @Inject
    public OneMinuteAggregator(final MetricWriter metricWriter, final DateTimeProvider dateTimeProvider) {
        this.metricWriter = metricWriter;
        this.dateTimeProvider = dateTimeProvider;
    }

    /**
     * Reads an incoming metric.
     *
     * The metric is enqueued and will be persisted when run() is called.
     *
     * @param metric the incoming metric.
     */
    public void readMetrics(final List<Metric> metrics) {
        metricsQueue.addAll(metrics);
    }

    protected long floorTimestamp(long timestamp) {
        return (timestamp / MS_TO_MIN) * MS_TO_MIN;
    }

    protected long getBeforeTimeInMs() {
        long ret = dateTimeProvider.get().getMillis();

        return floorTimestamp(ret - MS_TO_MIN);
    }

    /**
     * Aggregates the metrics and perists them to storage.
     */
    @Override
    public void run() {
        LOG.debug("Running aggregator...");

        // we need this try - catch block so events
        // will continue to run even with exceptions
        try {
            // fetch metrics from the queue
            final List<Metric> metrics = getAllMetrics();

            // make sure we have work to do
            if(metrics.size() == 0) {
                return;
            }

            LOG.debug("Aggregating {} metrics", metrics.size());

            final Multimap<MetricId, MetricValue> aggregatedMetrics = HashMultimap.create();

            // sort through all the metrics
            for(Metric metric:metrics) {
                aggregatedMetrics.put(metric.getId(), metric.getMetricValue());
            }

            final List<Metric> metricsToWrite = new ArrayList<Metric>(metrics.size());

            // go through and do the aggregation and write the metrics
            for(Map.Entry<MetricId, Collection<MetricValue>> metric:aggregatedMetrics.asMap().entrySet()) {
                final MetricValue value = metric.getValue().iterator().next();
                final double size = metric.getValue().size();

                if(size > 1) {
                    double sum = 0.0;

                    for(MetricValue v:metric.getValue()) {
                        sum += v.getValue();
                    }

                    value.setValue(sum / size);
                }

                // make sure we've floored the timestamp
                value.setTimestamp(floorTimestamp(value.getTimestamp()));

                // add the metric to our list to eventually write
                metricsToWrite.add(new Metric(metric.getKey(), value));
            }

            // write all the metrics at once
            metricWriter.writeMetrics(metricsToWrite);

        } catch(Exception e) {
            LOG.error("Caught top-level exception: {}", e.getMessage(), e);
        }
    }

    /**
     * Removes all of the metrics from the metrics queue.
     *
     * This method *should* work for the following reasons:
     * 1) it should complete before another invocation from run()
     * 2) any items added the metrics while this is called will be added
     *    to the end of the queue.
     * @return A list of metrics that were in the queue.
     */
    private List<Metric> getAllMetrics() {
        final List<Metric> ret = new ArrayList<Metric>(); // we don't set the size because it's O(n) for metrics
        Metric m = metricsQueue.poll();

        while(m != null) {
            ret.add(m);
            m = metricsQueue.poll();
        }

        return ret;
    }

}
