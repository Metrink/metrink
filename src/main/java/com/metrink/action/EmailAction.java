package com.metrink.action;

import static com.metrink.utils.MilliSecondUtils.msToQueryTime;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;

public class EmailAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(EmailAction.class);

    private final Provider<HtmlEmail> emailProvider;
    private Provider<VelocityContext> velocityContextProvider;
    private Template plainTextTemplate;
    private Template htmlTemplate;

    @Inject
    public EmailAction(final Provider<HtmlEmail> emailProvider,
                       final Provider<VelocityContext> velocityContextProvider,
                       @Named("email-template-plain-text") final Template plainTextTemplate,
                       @Named("email-template-html")       final Template htmlTemplate) {
        this.emailProvider = emailProvider;
        this.velocityContextProvider = velocityContextProvider;
        this.plainTextTemplate = plainTextTemplate;
        this.htmlTemplate = htmlTemplate;
    }

    @Override
    public void triggerAction(final Metric metric, final AlertBean alertBean, final ActionBean actionBean) {

        final DateTime start = new DateTime(metric.getTimestamp() - TimeUnit.MINUTES.toMillis(30), DateTimeZone.UTC);
        final DateTime end = new DateTime(metric.getTimestamp(), DateTimeZone.UTC);

        final StringBuilder query = new StringBuilder();

        /*
         * This is totally wrong, but we don't know what timezone we're sending to :-(
         */
        query.append(msToQueryTime(start.getMillis(), DateTimeZone.forID("US/Eastern")));
        query.append(" to ");
        query.append(msToQueryTime(end.getMillis(), DateTimeZone.forID("US/Eastern")));
        query.append(" m(\"");
        query.append(metric.getDevice());
        query.append("\", \"");
        query.append(metric.getGroupName());
        query.append("\", \"");
        query.append(metric.getName());
        query.append("\")");

        final VelocityContext context = velocityContextProvider.get();
        context.put("metric",              metric);
        context.put("alertQuery",          alertBean.getAlertQuery());
        context.put("graphLink",           getLinkUri(query.toString()));
        context.put("graphLinkCorrelated", getLinkUri(query.toString() + " | corr"));

        final StringWriter plainText = new StringWriter();
        final StringWriter html      = new StringWriter();

        plainTextTemplate.merge(context, plainText);
        htmlTemplate.merge(context, html);

        final StringBuilder subjectBuilder = new StringBuilder()
                .append("[METRINK] Alert for ")
                .append(metric.getId().toString());

        try {
            final HtmlEmail email = emailProvider.get();
            email.setSubject(subjectBuilder.toString());
            email.addTo(actionBean.getValue());
            //email.setMsg(plainText.toString()); // for some reason we cannot set this in GAE :-(
            email.setHtmlMsg(html.toString()); // Unintuitively, order matters here. Make sure this comes last.

            System.out.println(html.toString());

            final String messageId = email.send();

            LOG.info("Sent message {}", messageId);

        } catch (final EmailException e) {
            LOG.error("Error sending email: {}", e.getMessage());
        }
    }

    /**
     * Get a link from the URI given a path.
     * @param query the query
     * @return the URI in string format
     */
    private String getLinkUri(final String query) {
        try {
            return UriTemplate.fromTemplate("https://www.metrink.com/graphing/{query}")
                    .set("query", query)
                    .expand()
                    .replaceAll("\"", "%22"); // doesn't seem to encode double-quotes

        } catch (VariableExpansionException | MalformedUriTemplateException e) {
            LOG.error("URI Template Exception: {}", e.getMessage());
            throw new IllegalStateException("URISyntaxException", e);
        }
    }
}
