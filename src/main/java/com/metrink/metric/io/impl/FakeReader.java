package com.metrink.metric.io.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricReader;

@Singleton
public class FakeReader implements MetricReader {

    public static final Logger LOG = LoggerFactory.getLogger(FakeReader.class);

    private final List<String> devices;
    private final List<String> groups;
    private final Map<String, ? extends Map<String, ? extends Generator>> names;

    private static interface Generator {
        public double getNext(final long time, final String device);
        public void reset();
    }
    private static class ContinuousGenerator implements Generator {
        private Random randomGenerator = new Random();
        private double range;
        private double startingValue;
        private double min;
        private double max;
        private double probabilityOfChange;

        private double previous;
        private double trendDelta;

        public ContinuousGenerator(final double startingValue,
                                   final double probabilityOfChange,
                                   final double range,
                                   final double min,
                                   final double max) {
            this.startingValue = startingValue;
            this.previous = startingValue;
            this.probabilityOfChange = probabilityOfChange;
            this.range = range;
            this.min = min;
            this.max = max;
        }

        @Override
        public double getNext(final long time, final String device) {
            randomGenerator.setSeed(time * device.hashCode());

            if (randomGenerator.nextInt(100) < 100 * probabilityOfChange) {
                trendDelta = trendDelta == 0 ? randomGenerator.nextInt((int)(1000 * 2 * range)) / 1000.0 - range : 0;
            }

            double result = trendDelta + previous;
            result = result > min ? result : min;
            result = result < max ? result : max;
            previous = result;
            return result;
        }

        @Override
        public void reset() {
            previous = startingValue;
            trendDelta = 0;
        }
    }

    @Inject
    public FakeReader() {
        devices = ImmutableList.of("sql01", "web01", "web02");
        groups  = ImmutableList.of("cpu", "disk", "memory", "network");
        names   = ImmutableMap.of(
            "cpu", ImmutableMap.of(
                "load", new ContinuousGenerator(0.1, 0.5, 0.1, 0, 1)),

            "disk", ImmutableMap.of(
                "/ free", new ContinuousGenerator(1024, 0.25, 1024, 0, 1024*1024)),

            "memory", ImmutableMap.of(
                "free memory", new ContinuousGenerator(1024, 0.01, 1024, 0, 1024*1024)),

            "network", ImmutableMap.of(
                "eth0 transmit rate", new ContinuousGenerator(1024, 0.25, 1024, 0, 1024*1024),
                "eth0 receive rate", new ContinuousGenerator(1024, 0.25, 1024, 0, 1024*1024))
            );
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Map<MetricId, MetricValueList> readMetrics(final List<MetricRequest> requests) {
        final Map<MetricId, MetricValueList> results = new HashMap<MetricId, MetricValueList>();

        for (final MetricRequest request : requests) {
            final MetricValueList values = new MetricValueList(request.getStart(), request.getEnd());

            final MetricId id = request.getId();
            final String group = id.getGroupName();
            final String name = id.getName();

            final Map<String, ? extends Generator> groupNames = names.get(group);
            if (!groupNames.containsKey(name)) {
                continue;
            }

            final Generator generator = groupNames.get(name);
            generator.reset();

            for (long time = request.getStart(); time <= request.getEnd(); time += TimeUnit.MINUTES.toMillis(1)) {
                final double value = generator.getNext(time, id.getDevice());
                values.addMetric(new Metric(request.getId(), time, value, null));
            }

            results.put(request.getId(), values);
        }

        return results;
    }

    @Override
    public MetricValueList readMetrics(final MetricId id, final long start, final long end) {
        return readMetrics(Arrays.asList(new MetricRequest(id, start, end))).get(id);
    }

/*
    @Override
    public List<MetricOwner> readOwners() {
        return owners;
    }

    @Override
    public List<String> readUniqueDevices(final MetricOwner owner) {
        return devices;
    }

    @Override
    public List<String> readUniqueGroups(final MetricOwner owner) {
        return groups;
    }

    @Override
    public List<String> readUniqueMetricNames(final MetricOwner owner, final String group) {
        return ImmutableList.copyOf(names.get(group).keySet());
    }
*/
}
