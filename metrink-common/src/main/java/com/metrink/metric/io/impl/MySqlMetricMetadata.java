package com.metrink.metric.io.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.metrink.metric.MetricId;
import com.metrink.metric.io.MetricMetadata;
import com.sop4j.dbutils.BatchExecutor;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.ArrayListHandler;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ColumnListHandler;

@Singleton
public class MySqlMetricMetadata implements MetricMetadata {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlMetricMetadata.class);

    private final QueryRunner runner;

    @Inject
    public MySqlMetricMetadata(final QueryRunner runner) {
        this.runner = runner;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<MetricId> readMetricIds() {
        List<MetricId> ret = Lists.newArrayList();

        try {
            ret = runner.query(
                        "select * from unique_metrics_view " +
                        "order by device, groupName, name")
                    .execute(new BeanListHandler<MetricId>(MetricId.class));

        } catch (final SQLException e) {
            LOG.error("Error fetching unique metrics: {}", e.getMessage(), e);
        }

        return ret;
    }

    @Override
    public List<String> readUniqueDevices() {
        List<String> ret = new ArrayList<String>();

        try {
            ret = runner.query("select device from metrics_devices order by device")
                        .execute(new ColumnListHandler<String>(1));
        } catch (final SQLException e) {
            LOG.error("Error fetching unique devices: {}", e.getMessage(), e);
        }

        return ret;
    }

    @Override
    public List<String> readUniqueGroups() {
        List<String> ret = new ArrayList<String>();

        try {
            ret = runner.query("select groupName from metrics_groups order by groupName")
                        .execute(new ColumnListHandler<String>(1));
        } catch (final SQLException e) {
            LOG.error("Error fetching unique groups: {}", e.getMessage(), e);
        }

        return ret;
    }

    @Override
    public List<String> readUniqueMetricNames(final String group) {
        List<String> ret = new ArrayList<String>();

        try {
            LOG.debug("Getting unique metrics for {} {}", group);

            ret = runner.query("select distinct name from unique_metrics_view where groupName = :group order by name")
                        .bind("group", group)
                        .execute(new ColumnListHandler<String>(1));

            LOG.debug("DONE: Getting unique metrics: {}", ret.size());

        } catch (final SQLException e) {
            LOG.error("Error fetching unique metrics: {}", e.getMessage(), e);
        }

        return ret;
    }

    @Override
    public List<MetricId> expandMetricId(MetricId id) {
        List<MetricId> ret = new ArrayList<MetricId>();

        // if there is nothing to expand, then just return this one
        if((!id.getDevice().contains("*")) &&
           (!id.getGroupName().contains("*")) &&
           (!id.getName().contains("*"))) {
               return Arrays.asList(id);
        }

        final String device = id.getDevice().replace("*", "%");
        final String group = id.getGroupName().replace("*", "%");
        final String name = id.getName().replace("*", "%");

        try {
            LOG.debug("Expanding metric: {}", id);

            ret = runner.query("select * from unique_metrics_view where device like :device and groupName like :group and name like :name order by device, groupName, name")
                    .bind("device", device)
                    .bind("group", group)
                    .bind("name", name)
                    .execute(new BeanListHandler<MetricId>(MetricId.class));

            LOG.debug("Fetched {} results for {}", ret.size(), id);
        } catch (final SQLException e) {
            LOG.error("Error fetching unique metrics: {}", e.getMessage(), e);
        }

        return ret;
    }

    @Override
    public List<MetricId> readUniqueMetrics() {
        final List<MetricId> ret = new ArrayList<MetricId>();
        List<Object[]> results = new ArrayList<Object[]>();

        try {
            results = runner.query("select device, groupName, name from unique_metrics_view")
                            .execute(new ArrayListHandler());
        } catch (final SQLException e) {
            LOG.error("Error reading unique metrics: {}", e.getMessage());
        }


        for(final Object[] res:results) {
            ret.add(new MetricId(res[0].toString(), res[1].toString(), res[2].toString()));
        }

        return ret;
    }

    @Override
    public boolean writeMetricIds(final Set<MetricId> metricIds) {
        BatchExecutor executor = null;

        if(metricIds.isEmpty()) {
            LOG.warn("Attempted to write empty set of metric IDs");
            return true;
        }

        try {
            executor = runner.batch("call add_metric_ids(:device, :group, :name)");
        } catch (final SQLException e) {
            LOG.error("Error creating BatchExecutor: {}", e.getMessage());
            return false;
        }

        for (final MetricId metricId : metricIds) {
            try {
                executor.bind("device", metricId.getDevice())
                        .bind("group", metricId.getGroupName())
                        .bind("name", metricId.getName())
                      .addBatch();

                LOG.debug("Added stmt MetricId {}: (device, group, name) -> ({}, {}, {})", metricId, metricId.getDevice(), metricId.getGroupName(), metricId.getName());
            } catch (final SQLException e) {
                LOG.error("Error adding batch: {}", e.getMessage(), e);
                return false;
            }
        }

        try {
            executor.execute();
        } catch (final SQLException e) {
            LOG.error("Error executing batch: {}", e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public int deleteMetricId(final MetricId metricId) {
        try {
            return runner.update("call delete_metric(:device, :group, :name)")
                    .bind("device", metricId.getDevice())
                    .bind("group", metricId.getGroupName())
                    .bind("name", metricId.getName())
                    .execute();
        } catch (SQLException e) {
            LOG.error("Error executing delete_metric({}): {}", metricId, e.getMessage());
            return 0;
        }
    }
}
