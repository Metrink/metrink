package com.metrink.alert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.metrink.action.ActionFactory;
import com.metrink.action.LogAction;
import com.metrink.grammar.alert.AlertQuery;
import com.sop4j.dbutils.QueryRunner;

public class AlertEngineTest {
    //private static final Logger LOG = LoggerFactory.getLogger(AlertEngineTest.class);

    AlertQuery aq1;
    AlertQuery aq2;
    AlertQuery aq3;

    @Mock EntityManager entityManager;
    @Mock ActionFactory actionFactory;
    @Mock QueryRunner queryRunner;

    AlertEngine alertEngine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(actionFactory.createAction(any(ActionBean.class))).thenReturn(new LogAction());

        alertEngine = new AlertEngine(queryRunner, actionFactory);
    }

    @After
    public void tearDown() throws Exception {
    }

    // @Test
    public void integrationTest() {
        alertEngine = new AlertEngine(queryRunner, actionFactory);

        alertEngine.getAction("test");
    }

    @Test
    public void testAddNewQueriesSameOwner() {
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(1)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(3)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(2)));

        final List<AlertQuery> queries = alertEngine.getActiveQueriesFor();

        assertEquals(3, queries.size());
        assertEquals(Integer.valueOf(1), queries.get(0).getAlertId());
        assertEquals(Integer.valueOf(2), queries.get(1).getAlertId());
        assertEquals(Integer.valueOf(3), queries.get(2).getAlertId());
    }

    @Test
    public void testUpdateQueriesSameOwner() {
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(1)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(3)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(2)));

        // then add them again in a different order
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(2)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(1)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(3)));

        final List<AlertQuery> queries = alertEngine.getActiveQueriesFor();

        assertEquals(Integer.valueOf(1), queries.get(0).getAlertId());
        assertEquals(Integer.valueOf(2), queries.get(1).getAlertId());
        assertEquals(Integer.valueOf(3), queries.get(2).getAlertId());
    }

    @Test
    public void testAddNewQueriesDifferentOwner() {
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(1)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(3)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(2)));
        alertEngine.addOrUpdateAlertQueries(Arrays.asList(new AlertQuery(5)));

        List<AlertQuery> queries = alertEngine.getActiveQueriesFor();

        assertEquals(2, queries.size());
        assertEquals(Integer.valueOf(1), queries.get(0).getAlertId());
        assertEquals(Integer.valueOf(2), queries.get(1).getAlertId());

        queries = alertEngine.getActiveQueriesFor();

        assertEquals(2, queries.size());
        assertEquals(Integer.valueOf(3), queries.get(0).getAlertId());
        assertEquals(Integer.valueOf(5), queries.get(1).getAlertId());
}

}
