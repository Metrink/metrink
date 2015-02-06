package com.metrink.grammar.graph;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.metrink.grammar.ASTWalker;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.Query;
import com.metrink.grammar.javacc.MetrinkParser;
import com.metrink.grammar.javacc.ParseException;
import com.metrink.grammar.javacc.SimpleNode;
import com.metrink.grammar.javacc.TokenMgrError;

/**
 * Given an owner and a string, parses the string and creates a {@link GraphQuery} object.
 */
public class GraphQueryParser {
    public static final Logger LOG = LoggerFactory.getLogger(GraphQueryParser.class);

    private final Provider<GraphQueryVisitor> queryVisitorFactory;

    @Inject
    public GraphQueryParser(final Provider<GraphQueryVisitor> queryVisitorFactory) {
        this.queryVisitorFactory = queryVisitorFactory;
    }

    /**
     * Creates a {@link GraphQuery} given an owner, query, and start & end times.
     * @param queryString the query of the search.
     * @param startTime the start time for the query.
     * @param endTime the end time for the query.
     * @return A newly created unexecuted {@link GraphQuery}.
     * @throws MetrinkParseException
     */
    public GraphQuery createGraphQuery(final String queryString) throws MetrinkParseException {
        final GraphQueryVisitor visitor = queryVisitorFactory.get();
        final ASTWalker walker = new ASTWalker(visitor);

        // create our parser & parse the command line
        final MetrinkParser parser = new MetrinkParser(queryString);

        SimpleNode node;

        try {
            node = parser.CompilationUnit();
        } catch(ParseException e) {
            LOG.error("Error parsing query {}: {}", queryString, e.getMessage());
            LOG.debug("MetrinkParseException: ", e);
            throw new MetrinkParseException(e.getMessage());
        } catch(TokenMgrError e) {
            LOG.error("Error parsing query {}: {}", queryString, e.getMessage());
            LOG.debug("MetrinkParseException: ", e);
            throw new MetrinkParseException(e.getMessage());
        }

        try {
            walker.walk(node); // walk the AST
        } catch(IllegalArgumentException e) {
            throw new MetrinkParseException(e);
        }

        final Query query = visitor.getQuery();

        // make sure the query is of the right type
        if(!(query instanceof GraphQuery)) {
            throw new MetrinkParseException("Invalid query for a graph query");
        }

        final long startTime = ((GraphQuery)query).getStartTime();
        final long endTime = ((GraphQuery)query).getEndTime();

        // make sure the times are correct
        if(startTime >= endTime) {
            throw new MetrinkParseException("Start time after end time");
        }

        if(LOG.isDebugEnabled()) {
            final DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm a (zZ)");

            LOG.debug("START: {} ({})", format.print(startTime), startTime);
            LOG.debug("  END: {} ({})", format.print(endTime), endTime);
        }

        return (GraphQuery) query;
    }

}
