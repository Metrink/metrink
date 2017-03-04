package com.metrink.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.inject.DateTimeProvider;
import com.metrink.metric.Metric;

public class MetrinkJsonParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(MetrinkJsonParserTest.class);

    MetrinkJsonParser parser;

    DateTime CURRENT_TIME = new DateTime();
    @Mock DateTimeProvider dateTimeProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(dateTimeProvider.get()).thenReturn(CURRENT_TIME);

        parser = new MetrinkJsonParser(dateTimeProvider);
    }

    @Test
    public void testValidJson1() throws Exception {
        String line = "{'d':'db-server','m':[{'g':'mysql','n':'Bytes_received','v':'3506'},{'g':'mysql','n':'Bytes_sent','v':'34782'}]}";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(2, metrics.size());
        assertEquals("db-server", metrics.get(0).getDevice());
        assertEquals("mysql", metrics.get(0).getGroupName());
        assertEquals("Bytes_received", metrics.get(0).getName());
        assertEquals(3506, metrics.get(0).getValue(), 0.0001);

        assertEquals("db-server", metrics.get(1).getDevice());
        assertEquals("mysql", metrics.get(1).getGroupName());
        assertEquals("Bytes_sent", metrics.get(1).getName());
        assertEquals(34782, metrics.get(1).getValue(), 0.0001);
    }

    @Test
    public void testValidJson2() throws Exception {
        String line = "{'m':[{ 't': 123454789, 'g': 'mygroup', 'n':'myname', 'v':23.45 }], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(1, metrics.size());
        assertEquals("mydevice", metrics.get(0).getDevice());
        assertEquals("mygroup", metrics.get(0).getGroupName());
        assertEquals("myname", metrics.get(0).getName());
        assertEquals(23.45, metrics.get(0).getValue(), 0.0001);
    }

    @Test
    public void testValidJson3() throws Exception {
        String line = "{'m':[ ], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(0, metrics.size());
    }

    @Test
    public void testValidJson4() throws Exception {
        String line = "{'m':[{ 't': 123454789, 'g': 'mygroup', 'n':'myname', 'v':'23.45' }], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(1, metrics.size());
        assertEquals("mydevice", metrics.get(0).getDevice());
        assertEquals("mygroup", metrics.get(0).getGroupName());
        assertEquals("myname", metrics.get(0).getName());
        assertEquals(23.45, metrics.get(0).getValue(), 0.0001);
    }

    @Test
    public void testValidJson5() throws Exception {
        String line = "{'m':[{ 't': 123454789, 'g': 'mygroup', 'n':myname, 'v':23.45 }], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(1, metrics.size());
        assertEquals("mydevice", metrics.get(0).getDevice());
        assertEquals("mygroup", metrics.get(0).getGroupName());
        assertEquals("myname", metrics.get(0).getName());
        assertEquals(23.45, metrics.get(0).getValue(), 0.0001);
    }

    @Test
    public void testValidJson6() throws Exception {
        String line = "{'m':[{ 't': 123454789, 'g': mygroup, 'n':'myname', 'v':23.45 }], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(1, metrics.size());
        assertEquals("mydevice", metrics.get(0).getDevice());
        assertEquals("mygroup", metrics.get(0).getGroupName());
        assertEquals("myname", metrics.get(0).getName());
        assertEquals(23.45, metrics.get(0).getValue(), 0.0001);
    }

    @Test
    public void testValidJson7() throws Exception {
        String line = "{'m':[{ 't': '123454789', 'g': 'mygroup', 'n':'myname', 'v':23.45 }], 'd': 'mydevice' }";

        List<Metric> metrics = parser.parse(line.getBytes());

        assertEquals(1, metrics.size());
        assertEquals("mydevice", metrics.get(0).getDevice());
        assertEquals("mygroup", metrics.get(0).getGroupName());
        assertEquals("myname", metrics.get(0).getName());
        assertEquals(23.45, metrics.get(0).getValue(), 0.0001);
    }

    @Test(expected=ParserException.class)
    public void testInvalidJson1() throws Exception {
        String line = "{'m':[{ 't': 123454789, 'g': 'mygroup', 'n':'myname', 'v':23.45 }] }";

        List<Metric> metrics = parser.parse(line.getBytes());
    }

}
