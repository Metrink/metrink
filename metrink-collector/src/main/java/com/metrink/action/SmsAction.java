package com.metrink.action;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;

/**
 * The base action for all SMS actions.
 *
 * A list of gateways can be found here: http://www.emailtextmessages.com/
 *
 */
public abstract class SmsAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(SmsAction.class);

    private final Provider<SimpleEmail> emailProvider;

    public SmsAction(final Provider<SimpleEmail> emailProvider) {
        this.emailProvider = emailProvider;
    }

    @Override
    public void triggerAction(Metric metric, AlertBean alertBean, ActionBean actionBean) {
        final String toAddr = constructAddress(actionBean.getValue());
        final String alertQuery = alertBean.getAlertQuery().substring(0, alertBean.getAlertQuery().lastIndexOf(" do "));
        final StringBuilder sb = new StringBuilder();

        sb.append(metric.getId());
        sb.append(" ");
        sb.append(metric.getValue());
        sb.append(" triggered ");
        sb.append(alertQuery);

        try {
            final SimpleEmail email = emailProvider.get();

            email.addTo(toAddr);
            email.setSubject("METRINK Alert");
            email.setMsg(sb.toString());

            final String messageId = email.send();

            LOG.info("Sent message {} to {}", messageId, toAddr);
        } catch (final EmailException e) {
            LOG.error("Error sending email: {}", e.getMessage());
        }
    }

    /**
     * Given a phone number, create the address for the gateway.
     * @param phoneNumber the phone number.
     * @return the email address to use.
     */
    protected abstract String constructAddress(String phoneNumber);
}
