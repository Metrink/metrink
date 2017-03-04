package com.metrink.action;

import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;

public interface Action {

    public void triggerAction(Metric metric, AlertBean alertBean, ActionBean actionBean);
}
