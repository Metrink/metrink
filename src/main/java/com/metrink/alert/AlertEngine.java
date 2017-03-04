package com.metrink.alert;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.metrink.action.ActionFactory;
import com.metrink.grammar.alert.AlertQuery;
import com.metrink.metric.Metric;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanHandler;

/**
 * The alert engine that processes metrics as they come in and triggers alerts as needed.
 *
 * There should only be a single instance of this.
 */
@Singleton
public class AlertEngine {
    private static final Logger LOG = LoggerFactory.getLogger(AlertEngine.class);

    private static final AlertQueryComparator ALERT_ID_COMPARATOR = new AlertQueryComparator();

    /**
     * This is essentially an in-memory copy of the alerts table with all queries parsed.
     * Row keys are MetricOwner, columns are the AlertId, and values are the AlertQuery.
     */
    private final Map<Integer, AlertQuery> activeQueries = new HashMap<Integer, AlertQuery>();
    private final ReentrantReadWriteLock activeQueriesLock = new ReentrantReadWriteLock();

    private final QueryRunner queryRunner;
    private final ActionFactory actionFactory;

    @Inject
    public AlertEngine(final QueryRunner queryRunner,
                       final ActionFactory actionFactory) {
        this.queryRunner = queryRunner;
        this.actionFactory = actionFactory;
    }

    /**
     * Processes a list of metrics by routing them to the appropriate {@link AlertQuery} objects.
     * @param metrics the list of metrics.
     */
    public void processMetrics(final List<Metric> metrics) {
        final Collection<AlertQuery> alertQueries = activeQueries.values();

        LOG.debug("Searching {} alert querires", alertQueries.size());

        for(final Metric metric:metrics) {
            for(final AlertQuery alertQuery:alertQueries) {
                // see if we have a match
                if(alertQuery.getMetricId().matches(metric.getId())) {
                    // see if we need to trigger the action
                    if(alertQuery.processMetric(metric)) {
                        LOG.debug("Triggering alert {} for {}", alertQuery, metric);

                        final ActionBean actionBean = getAction(alertQuery.getActionName());
                        AlertBean alertBean = null;

                        try {
                            alertBean = queryRunner.query("select * from alerts where alertId = :alertId")
                                                   .bind("alertId", alertQuery.getAlertId())
                                                   .execute(new BeanHandler<AlertBean>(AlertBean.class));
                        } catch (final SQLException e) {
                            LOG.error("Error reading alert bean: {}", e.getMessage());
                            continue;
                        }

                        // create and trigger the action
                        actionFactory.createAction(actionBean).triggerAction(metric, alertBean, actionBean);

                        LOG.debug("Created action {}", actionBean);
                    }
                }
            }
        }
    }

    /**
     * Given an owner and action name, gets the action from the DB.
     * @param actionName the name of the action.
     * @return the {@link ActionBean} for this action.
     */
    public ActionBean getAction(final String actionName) {
        ActionBean ret = null;

        try {
            ret = queryRunner.query("SELECT * FROM actions WHERE actionName = :actionName")
                .bind("actionName", actionName)
                .execute(new BeanHandler<ActionBean>(ActionBean.class));
        } catch(final SQLException e) {
            LOG.error("Error fetching action: {}", e.getMessage());
            return null;
        }

        return ret;
    }

    /**
     * Adds or updates the queries.
     * @param queries the queries to add or update.
     */
    public void addOrUpdateAlertQueries(final List<AlertQuery> queries) {
        // get the write lock
        activeQueriesLock.writeLock().lock();

        try {
            // go through the queries
            for(final AlertQuery query:queries) {
                // remove the query from the table
                final AlertQuery oldQuery = activeQueries.remove(query.getAlertId());

                if(LOG.isDebugEnabled()) {
                    if(oldQuery != null) {
                        LOG.debug("Updating query: {}", query);
                    } else {
                        LOG.debug("Adding query: {}", query);
                    }
                }

                // put the query into the table updating or adding
                activeQueries.put(query.getAlertId(), query);
            }
        } finally {
            // unlock the write lock regardless of exceptions, etc
            activeQueriesLock.writeLock().unlock();
        }
    }

    /**
     * Remove the given alert from the active table.
     * @param alertId the ID of the alert.
     * @return true if the alert was removed.
     */
    public boolean removeQuery(final Integer alertId) {
        // get the write lock
        activeQueriesLock.writeLock().lock();

        try {
            return activeQueries.remove(alertId) != null;
        } finally {
            // unlock the write lock regardless of exceptions, etc
            activeQueriesLock.writeLock().unlock();
        }
    }

    /**
     * Returns a list of {@link AlertQuery} sorted by id for the given owner.
     * @return the {@link AlertQuery}s sorted by id.
     */
    public List<AlertQuery> getActiveQueriesFor() {
        // get the read lock
        activeQueriesLock.readLock().lock();

        try {
            final List<AlertQuery> ret = Lists.newArrayList(activeQueries.values());

            // sort the list
            Collections.sort(ret, ALERT_ID_COMPARATOR);

            return ImmutableList.copyOf(ret);
        } finally {
            // unlock the read lock regardless of exceptions, etc
            activeQueriesLock.readLock().unlock();
        }
    }

    /**
     * A {@link Comparator} that compares alert IDs.
     */
    private static class AlertQueryComparator implements Comparator<AlertQuery> {
        @Override
        public int compare(final AlertQuery aq1, final AlertQuery aq2) {
            return Integer.compare(aq1.getAlertId(), aq2.getAlertId());
        }
    }

}
