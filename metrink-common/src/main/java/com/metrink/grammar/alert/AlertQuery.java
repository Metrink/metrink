package com.metrink.grammar.alert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.metrink.grammar.Comparator;
import com.metrink.grammar.Query;
import com.metrink.grammar.TriggerExpression;
import com.metrink.grammar.query.QueryNode;
import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;

/**
 * This class represents a parsed alert query that is also associated with a line in the alerts table.
 */
public class AlertQuery implements Serializable, Query {
    private static final Logger LOG = LoggerFactory.getLogger(AlertQuery.class);

    private static final long serialVersionUID = 1L;

    private Integer alertId; // this is ID of the alert in the DB

    private final MetricId metricId; // this can/will change later
    private final double targetValue;
    private final long duration;
    private final Comparator comparator;
    private final String actionName;

    // the first time the target value has been met for each metric
    private final Map<MetricId, DateTime> firstOccurrenceMap = new HashMap<>();

    /**
     * Default constructor for creating objects from Hibernate.
     */
    public AlertQuery() {
        this.metricId = null;
        this.targetValue = 0.0;
        this.duration = 0;
        this.comparator = null;
        this.actionName = null;
    }

    /**
     * For debugging only.
     * @param id the ID of the alert
     * @param owner the owner.
     */
    public AlertQuery(int id) {
        this.alertId = id;
        this.metricId = null;
        this.targetValue = 0.0;
        this.duration = 0;
        this.comparator = null;
        this.actionName = null;
    }

    @Inject
    public AlertQuery(@Assisted QueryNode graphQuery,
                      @Assisted TriggerExpression trigger,
                      @Assisted String actionName) {
        if(! (graphQuery instanceof MetricIdNode)) {
            LOG.warn("The query must only be a metric: {}", graphQuery.getClass().getCanonicalName());
            throw new IllegalArgumentException("The query must only be a metric");
        }

        this.metricId = ((MetricIdNode)graphQuery).getMetricId();
        this.targetValue = trigger.getNumberArg().getDouble();
        this.duration = trigger.getTimeArg() == null ? 0 : trigger.getTimeArg().getTimeInMs();
        this.comparator = trigger.getComparator();
        this.actionName = actionName;
    }

    /**
     * Processes a given metric checking if the action should fire.
     * @param metric the metric to process.
     * @return true if the action should fire.
     */
    public boolean processMetric(Metric metric) {
        final double value = metric.getValue();
        final DateTime metricTime = new DateTime(metric.getTimestamp());
        final DateTime firstOccurrence = firstOccurrenceMap.get(metric.getId());

        // see if the comparator is satisfied
        if(comparator.compare(value, targetValue)) {
            // if there is not time arg, then just return true
            if(duration == 0) {
                return true;
            }

            // otherwise we have to check the time
            if(firstOccurrence != null) {
                if(metricTime.isAfter(firstOccurrence.plus(duration))) {
                    return true;
                }
            } else {
                // set this as the first occurrence (make a copy so we don't keep the reference)
                firstOccurrenceMap.put(metric.getId(), new DateTime(metric.getTimestamp()));
            }
        } else {
            // reset the first occurrence
            firstOccurrenceMap.remove(metric.getId());
        }

        // all other cases fall through to returning false
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(metricId);
        sb.append(" ");
        sb.append(comparator);
        sb.append(" ");
        sb.append(targetValue);
        sb.append(" for ");
        sb.append(duration);
        sb.append(" do ");
        sb.append(actionName);

        return sb.toString();
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public MetricId getMetricId() {
        return metricId;
    }

    public String getActionName() {
        return actionName;
    }

    /**
     * Interface for creating AlertQuery objects.
     */
    public interface AlertQueryFactory {
        public AlertQuery create(QueryNode graphQuery, TriggerExpression trigger, String actionName);
    }

}
