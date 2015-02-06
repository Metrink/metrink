package com.metrink.gui;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.MDC;

/**
 * {@link org.apache.wicket.request.cycle.IRequestCycleListener} which adds the session and user to the log
 */
public class MetrinkMdcLogListener extends AbstractRequestCycleListener {
    @Override
    public void onBeginRequest(final RequestCycle cycle) {
        super.onBeginRequest(cycle);

        final MetrinkSession session = (MetrinkSession)WebSession.get();

        MDC.put("sessionId", session.getId());
        MDC.put("user",      session.getUsername());
    }
}