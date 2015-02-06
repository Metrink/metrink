package com.metrink.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.mail.SimpleEmail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.util.Providers;
import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.metric.Metric;
import com.metrink.metric.MetricId;

public class SmsActionTest {
    private static final Logger LOG = LoggerFactory.getLogger(SmsActionTest.class);

    SmsAction sms;
    @Mock SimpleEmail email;

    MetricId id = new MetricId("long-device-name", "group-name", "metric-name");
    Metric metric = new Metric(id, 0, 12345.6789, null);
    AlertBean alertBean = new AlertBean();
    ActionBean actionBean = new ActionBean();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        alertBean.setAlertQuery("m('long-device-name', 'group-name', 'metric-name') > 123456.789 for 30m do test");
        actionBean.setValue("phone number");

        sms = new SmsAction(Providers.of(email)) {
            @Override
            protected String constructAddress(String phoneNumber) {
                return phoneNumber;
            }
        };
    }

    @Test
    public void test() throws Exception {
        sms.triggerAction(metric, alertBean, actionBean);

        verify(email, times(1)).addTo(eq("phone number"));
        verify(email, times(1)).setSubject(eq("METRINK Alert"));
        verify(email, times(1)).setMsg(any(String.class));
        verify(email, times(1)).send();
    }
}
