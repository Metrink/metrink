package com.metrink.alert;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.metrink.grammar.alert.AlertQueryParser;
import com.sop4j.dbutils.QueryRunner;

public class AlertTablePollerTest {
    //private static final Logger LOG = LoggerFactory.getLogger(AlertTablePollerTest.class);

    @Mock QueryRunner queryRunner;
    @Mock AlertEngine alertEngine;
    @Mock AlertQueryParser alertQueryParser;

    AlertTablePoller tablePoller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        tablePoller = new AlertTablePoller(queryRunner, alertEngine, alertQueryParser);
    }

    @After
    public void tearDown() throws Exception {
    }

    //@Test
    public void test() {
        tablePoller.run();

        //fail("Not yet implemented");
    }
}
