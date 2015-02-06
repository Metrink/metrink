package com.metrink.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;

public class LogAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(LogAction.class);

    @Override
    public void triggerAction(Metric metric, AlertBean alertBean, ActionBean actionBean) {
        LOG.warn("{} triggered {}", metric, alertBean.getAlertQuery());
    }

}
