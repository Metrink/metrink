package com.metrink.grammar.graph;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.metrink.grammar.ASTWalker.WalkType;
import com.metrink.grammar.BaseNode;
import com.metrink.grammar.BaseQueryVisitor;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.RelativeTimeArgument;
import com.metrink.grammar.graph.GraphQuery.GraphQueryFactory;
import com.metrink.grammar.graph.MetricFunction.MetricFunctionFactory;
import com.metrink.grammar.javacc.ASTAlertQuery;
import com.metrink.grammar.javacc.ASTGraphQuery;
import com.metrink.grammar.javacc.ASTMetric;
import com.metrink.grammar.javacc.ASTRelativeTimeArray;
import com.metrink.grammar.javacc.ASTRelativeTimeLiteral;
import com.metrink.grammar.javacc.ASTTriggerExpression;
import com.metrink.grammar.javacc.SimpleNode;
import com.metrink.grammar.query.ConnectorNode;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.grammar.query.QueryNode;
import com.metrink.gui.MetrinkSession;
import com.metrink.metric.MetricId;
import com.metrink.utils.MilliSecondUtils;

/**
 * An AST Visitor that walks Query ASTs.
 */
public class GraphQueryVisitor extends BaseQueryVisitor {

    private final static Logger LOG = LoggerFactory.getLogger(GraphQueryVisitor.class);

    private final MetricFunctionFactory metricFunctionFactory;
    private final GraphQueryFactory graphQueryFactory;

    // for injection only
    public GraphQueryVisitor() {
        super();
        this.metricFunctionFactory = null;
        this.graphQueryFactory = null;
    }

    @Inject
    public GraphQueryVisitor(final MetricFunctionFactory metricFunctionFactory,
                             final QueryFunctionFactory queryFunctionFactory,
                             final GraphQueryFactory graphQueryFactory) {
        super(queryFunctionFactory);
        this.graphQueryFactory = graphQueryFactory;
        this.metricFunctionFactory = metricFunctionFactory;
    }

    @Override
    protected DateTimeZone getTimeZone() {
        return DateTimeZone.forID(MetrinkSession.getCurrentUser().getTimezone());
    }

    @Override
    public WalkType visit(final ASTAlertQuery node, final Boolean secondVisit) throws MetrinkParseException {
        throw new MetrinkParseException("This is an alert query, not a graph query");
    }

    @Override
    public WalkType visit(final ASTTriggerExpression node, final Boolean secondVisit) throws MetrinkParseException {
        throw new MetrinkParseException("This is an alert query, not a graph query");
    }

    @Override
    public WalkType visit(final ASTGraphQuery node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        long start = -1;
        long end = -1;
        final DateTime NOW = DateTime.now(getTimeZone()); // construct NOW using the user's timezone
        QueryNode rootNode;

        //
        // Figure out the start and end time
        //
        final Object startValue = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();

        if(startValue instanceof RelativeTimeArgument) {
            final long timeArg = ((RelativeTimeArgument) startValue).getTimeInMs();

            if(timeArg >= 0) {
                throw new MetrinkParseException("Invalid relative time, must be negative " + startValue);
            }

            // we don't need to convert via time zone because it's already in UTC
            start = NOW.getMillis() + timeArg;

        } else if(startValue instanceof DateTime) {
            // nothing to convert here, as the DateTime was created with the user's timezone
            start = ((DateTime) startValue).getMillis();
        } else if(startValue instanceof LocalTime) {
            final MutableDateTime time = NOW.toMutableDateTime();

            time.setTime(((LocalTime) startValue).getHourOfDay(),
                         ((LocalTime) startValue).getMinuteOfHour(),
                         0, 0); // we don't have seconds or ms

            start = time.getMillis();
        } else {
            throw new MetrinkParseException("Unknown start time: " + startValue);
        }

        if(node.getImage() != null) { // we have 2 times
            final Object endValue = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();

            if(endValue instanceof DateTime) {
                end = ((DateTime) endValue).getMillis();
            } else if(endValue instanceof LocalTime) {
                MutableDateTime time = new MutableDateTime(start, getTimeZone());

                time.setTime(((LocalTime) endValue).getHourOfDay(),
                            ((LocalTime) endValue).getMinuteOfHour(),
                            0, 0); // we don't have seconds or ms

                // convert from the user's time zone to UTC
                end = time.getMillis();
            }

            // get the root node from the expression
            rootNode = (QueryNode) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue();
        } else { // just one, so set the ending to now
            // no need to convert, as this is in UTC
            end = NOW.getMillis();

            // get the root node from the expression
            rootNode = (QueryNode) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();
        }

        // round the start down and the end up
        start = MilliSecondUtils.roundDown1Minute(start);
        end = MilliSecondUtils.roundUp1Minute(end);

        final GraphQuery graphQuery = graphQueryFactory.create(rootNode, start, end);

        LOG.trace("Graph Query: {}", graphQuery);

        node.jjtSetValue(graphQuery);

        return WalkType.POST_CHILDREN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WalkType visit(final ASTMetric node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final int numChildren = node.jjtGetNumChildren();

        if(numChildren < 3) {
            throw new MetrinkParseException("A metric must have at least 3 arguments (device, group, name), had " + numChildren);
        }

        // convert everything into a String[]
        final String[] devices = node2StringArray((SimpleNode) node.jjtGetChild(0));
        final String[] groups  = node2StringArray((SimpleNode) node.jjtGetChild(1));
        final String[] names   = node2StringArray((SimpleNode) node.jjtGetChild(2));

        List<RelativeTimeArgument> relativeTimeArgs = Arrays.asList();

        if(numChildren == 4) {
            final BaseNode baseNode = (BaseNode)node.jjtGetChild(3);

            if(baseNode instanceof ASTRelativeTimeLiteral) {
                relativeTimeArgs = Arrays.asList((RelativeTimeArgument) baseNode.jjtGetValue());
            } else if(baseNode instanceof ASTRelativeTimeArray) {
                relativeTimeArgs = (List<RelativeTimeArgument>) baseNode.jjtGetValue();
            } else {
                throw new MetrinkParseException("Unknown metric argument type: " + baseNode.getClass().getCanonicalName());
            }
        }

        // create the list of metric functions
        final List<MetricFunction> funs = Lists.newArrayList();

        for(String device:devices) {
            for(String group:groups) {
                for(String name:names) {
                    final MetricId id = new MetricId(device, group, name);
                    final MetricFunction fun = metricFunctionFactory.create(id, relativeTimeArgs);

                    LOG.trace("Metric function: {}", fun);

                    funs.add(fun);
                }
            }
        }

        // if we only have one, just set it and leave
        if(funs.size() == 1) {
            node.jjtSetValue(funs.get(0));
            return WalkType.POST_CHILDREN;
        }

        // otherwise we need to build up a the tree
        ConnectorNode prevConnector = new ConnectorNode(funs.get(0), funs.get(1), ConnectorNode.Type.COPY);

        for(int i=2; i < funs.size(); ++i) {
            prevConnector = new ConnectorNode(prevConnector, funs.get(i), ConnectorNode.Type.COPY);
        }

        node.jjtSetValue(prevConnector);

        return WalkType.POST_CHILDREN;
    }

}
