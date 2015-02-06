package com.metrink.grammar.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.metrink.alert.AlertBean;
import com.metrink.grammar.ASTWalker;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.Query;
import com.metrink.grammar.javacc.MetrinkParser;
import com.metrink.grammar.javacc.ParseException;
import com.metrink.grammar.javacc.SimpleNode;
import com.sop4j.dbutils.QueryRunner;

public class AlertQueryParser {
    public static final Logger LOG = LoggerFactory.getLogger(AlertQueryParser.class);

    private final Provider<AlertQueryVisitor> queryVisitorFactory;

    @Inject
    public AlertQueryParser(final Provider<AlertQueryVisitor> queryVisitorFactory) {
        this.queryVisitorFactory = queryVisitorFactory;
    }

    /**
     * Create an {@link AlertQuery} from a string doing all the parsing work.
     * @param queryString the query of the search.
     * @return A newly created {@link AlertQuery}.
     * @throws MetrinkParseException
     */
    public AlertQuery createAlertQuery(final String queryString) throws MetrinkParseException {
        final AlertQueryVisitor visitor = queryVisitorFactory.get();
        final ASTWalker walker = new ASTWalker(visitor);

        // create our parser & parse the command line
        final MetrinkParser parser = new MetrinkParser(queryString);

        SimpleNode node;

        try {
            node = parser.CompilationUnit();
        } catch(ParseException e) {
            LOG.error("Error parsing query {}: {}", queryString, e.getMessage());
            LOG.debug("MetrinkParseException: ", e);
            throw new MetrinkParseException(e);
        }

        try {
            walker.walk(node); // walk the AST
        } catch(IllegalArgumentException e) {
            throw new MetrinkParseException(e);
        }

        final Query query = visitor.getQuery();

        // make sure the query is of the right type
        if(!(query instanceof AlertQuery)) {
            throw new MetrinkParseException("Invalid query for an alert");
        }

        return (AlertQuery) query;
    }

    /**
     * Creates an {@link AlertQuery} from an {@link AlertBean}.
     * @param alert the alert to parse.
     * @return a newly created AlertQuery.
     * @throws MetrinkParseException
     */
    public AlertQuery createAlertQuery(final QueryRunner queryRunner, final AlertBean alert) throws MetrinkParseException {
        final AlertQuery ret = createAlertQuery(alert.getAlertQuery());

        ret.setAlertId(alert.getAlertId());

        return ret;
    }
}
