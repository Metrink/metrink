package com.metrink.alert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.alert.AlertQuery;
import com.metrink.grammar.alert.AlertQueryParser;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;

/**
 * Pulls alerts from the alerts table updating the engine.
 */
public class AlertTablePoller implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AlertTablePoller.class);

    private final QueryRunner queryRunner;
    private final AlertEngine alertEngine;
    private final AlertQueryParser alertQueryParser;
    private Date lastRead; // we use this because that's what AlertBean uses

    @Inject
    public AlertTablePoller(final QueryRunner queryRunner,
                            final AlertEngine alertEngine,
                            final AlertQueryParser alertQueryParser) {

        this.queryRunner = queryRunner;
        this.alertEngine = alertEngine;
        this.alertQueryParser = alertQueryParser;
        lastRead = new Date(0); // set this to the beginning of time
    }

    @Override
    public void run() {
        try {
            final List<AlertBean> alerts = readAlertBeans(); // get all the beans from the DB
            final List<AlertQuery> queries = new ArrayList<>();

            if(alerts.size() != 0) {
                LOG.debug("Read {} alerts from the db", alerts.size());
            }

            // go through the beans parsing them into AlertQuery objects
            for(final AlertBean alert:alerts) {
                // if the alert is disabled, simply remove it from the active queries
                if(! alert.isEnabled()) {
                    final boolean removed = alertEngine.removeQuery(alert.getAlertId());

                    if(!removed) {
                        LOG.warn("Error removing disabled alert: {}", alert);
                    }
                } else {
                    // we have an enabled query that was added or updated
                    // parse it and put it into the list to add to the engine later
                    try {
                        queries.add(alertQueryParser.createAlertQuery(queryRunner, alert));
                    } catch (final MetrinkParseException e) {
                        LOG.error("Error parsing alert query: {}", e.getMessage());
                        continue; // just skip this one
                    }
                }
            }

            // add all of the queries to the engine
            alertEngine.addOrUpdateAlertQueries(queries);
        } catch(final Exception e) {
            // need to wrap this whole thing or else it won't continue to run
            LOG.error("Error running alert table poller: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<AlertBean> readAlertBeans() {
        List<AlertBean> alerts = ImmutableList.<AlertBean>of();

        try {
            alerts = queryRunner.query("select * from alerts where modifiedTime > :lastRead order by modifiedTime asc")
                                .bind("lastRead", lastRead)
                                .execute(new BeanListHandler<AlertBean>(AlertBean.class));

            if(alerts != null && alerts.size() > 0) {
                // get the time of the last one
                lastRead = alerts.get(alerts.size()-1).getModifiedTime();
                return alerts;
            } else {
                // never return null
                return ImmutableList.<AlertBean>of();
            }
        } catch(final SQLException e) {
            LOG.error("Error getting list of alerts: {}", e.getMessage());
            return ImmutableList.<AlertBean>of();
        }

    }
}
