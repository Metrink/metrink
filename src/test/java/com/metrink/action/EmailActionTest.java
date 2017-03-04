package com.metrink.action;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;

public class EmailActionTest {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(EmailActionTest.class);

    @Mock private HtmlEmail email;
    @Mock private VelocityContext velocity;
    @Mock private Provider<HtmlEmail> emailProvider;
    @Mock private Provider<VelocityContext> velocityProvider;
    @Mock private Template plainTextTemplate;
    @Mock private Template htmlTemplate;

    private Metric metric = new Metric("device", "group", "/ name", 0, 0, "");
    private AlertBean alertBean = new AlertBean();
    private ActionBean actionBean = new ActionBean();

    private EmailAction emailAction;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(emailProvider.get()).thenReturn(email);
        when(velocityProvider.get()).thenReturn(velocity);

        this.emailAction = new EmailAction(emailProvider, velocityProvider, plainTextTemplate, htmlTemplate);

        alertBean.setAlertQuery("QUERY");;
        actionBean.setValue("unit-test@metrink.com");
    }

    @Test
    public void testTemplating() throws EmailException, UnsupportedEncodingException {
        emailAction.triggerAction(metric, alertBean, actionBean);

        /*
        context.put("metric",              metric);
        context.put("alertQuery",          alertBean.getAlertQuery());
        context.put("graphLink",           getLinkUri(query));
        context.put("graphLinkCorrelated", getLinkUri(query + " | corr"));
        */

        final String graphLink = "https://www.metrink.com/graphing/"
            // This tests both standard encoding and confirm that the forward slash translates to a %2F
            // 1969-12-31 18:30 to 1969-12-31 19:00 m("device", "group", "/ name")
            + "1969-12-31%2018%3A30%3A00%20to%201969-12-31%2019%3A00%3A00%20m%28\"device\"%2C%20\"group\"%2C%20\"%2F%20name\"%29";

        verify(velocity, times(1)).put("metric", metric);
        verify(velocity, times(1)).put("alertQuery", "QUERY");
        verify(velocity, times(1)).put("graphLink", graphLink);
        verify(velocity, times(1)).put("graphLinkCorrelated", graphLink + "%20%7C%20corr");

        verify(plainTextTemplate, times(1)).merge(eq(velocity), any(StringWriter.class));
        verify(htmlTemplate,      times(1)).merge(eq(velocity), any(StringWriter.class));

        verify(email, times(1)).setSubject("[METRINK] Alert for device:group:/ name");
        verify(email, times(1)).addTo("unit-test@metrink.com");
        verify(email, times(1)).send();

        // Annoyingly, if you call setMsg after setHtmlMsg, the HTML message is discarded
        final InOrder inOrder = Mockito.inOrder(email, email);
        inOrder.verify(email, times(1)).setMsg(anyString());
        inOrder.verify(email, times(1)).setHtmlMsg(anyString());

    }
}
