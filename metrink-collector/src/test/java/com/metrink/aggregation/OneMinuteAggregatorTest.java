package com.metrink.aggregation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.metrink.inject.DateTimeProvider;
import com.metrink.metric.Metric;
import com.metrink.metric.io.MetricWriter;

public class OneMinuteAggregatorTest {
    //private static final Logger LOG = LoggerFactory.getLogger(OneMinuteAggregatorTest.class);

    DateTime CURRENT_TIME = new DateTime();
    OneMinuteAggregator aggregator;

    @Mock DateTimeProvider dateTimeProvider;
    @Mock MetricWriter metricWriter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(dateTimeProvider.get()).thenReturn(CURRENT_TIME);

        aggregator = new OneMinuteAggregator(metricWriter, dateTimeProvider);
    }

    @Test
    public void testNoMetrics() {
        aggregator.run();

        verify(metricWriter, never()).writeMetrics(anyList());
    }

    @Test
    public void testOneMetrics() {
        Metric m1 = new Metric();
        Metric m2 = new Metric();

        m1.setTimestamp(CURRENT_TIME.getMillis());
        m2.setTimestamp(CURRENT_TIME.getMillis());
        m1.setValue(23.45);
        m2.setValue(54.32);

        aggregator.readMetrics(Arrays.asList(m1, m2));

        aggregator.run();

        ArgumentCaptor<List> capture = ArgumentCaptor.forClass(List.class);

        verify(metricWriter, times(1)).writeMetrics(capture.capture());

        List<Metric> metrics = capture.getValue();

        assertEquals(1, metrics.size());
        assertEquals(77.77/2, metrics.get(0).getValue(), 0.001);
        assertEquals(aggregator.floorTimestamp(CURRENT_TIME.getMillis()), metrics.get(0).getTimestamp());
    }
}
