package com.metrink.grammar.alert;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.grammar.ASTWalker.WalkType;
import com.metrink.grammar.BaseNode;
import com.metrink.grammar.BaseQueryVisitor;
import com.metrink.grammar.Comparator;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.NumberArgument;
import com.metrink.grammar.RelativeTimeArgument;
import com.metrink.grammar.TriggerExpression;
import com.metrink.grammar.alert.AlertQuery.AlertQueryFactory;
import com.metrink.grammar.javacc.ASTAlertQuery;
import com.metrink.grammar.javacc.ASTConnector;
import com.metrink.grammar.javacc.ASTGraphQuery;
import com.metrink.grammar.javacc.ASTMetric;
import com.metrink.grammar.javacc.ASTRelativeTimeLiteral;
import com.metrink.grammar.javacc.ASTTriggerExpression;
import com.metrink.grammar.javacc.SimpleNode;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.grammar.query.QueryNode;
import com.metrink.metric.MetricId;

public class AlertQueryVisitor extends BaseQueryVisitor {
    public static final Logger LOG = LoggerFactory.getLogger(AlertQueryVisitor.class);

    private final AlertQueryFactory alertQueryFactory;

    public AlertQueryVisitor() {
        this.alertQueryFactory = null;
    }

    @Inject
    public AlertQueryVisitor(AlertQueryFactory alertQueryFactory,
                             QueryFunctionFactory queryFunctionFactory) {
        super(queryFunctionFactory);
        this.alertQueryFactory = alertQueryFactory;
    }

    @Override
    protected DateTimeZone getTimeZone() {
        // this *should* never be used in an alert, as there should never be dates
        return DateTimeZone.UTC;
    }

    @Override
    public WalkType visit(final ASTGraphQuery node, final Boolean secondVisit) throws MetrinkParseException {
        throw new MetrinkParseException("This is a graph query, not an alert query");
    }

    @Override
    public WalkType visit(final ASTAlertQuery node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        // we know the first 2 values will always be a GraphExpression & TriggerExpression
        final QueryNode queryNode = (QueryNode) ((BaseNode)node.jjtGetChild(0)).jjtGetValue();
        final TriggerExpression triggerExpression = (TriggerExpression)((BaseNode)node.jjtGetChild(1)).jjtGetValue();
        final String actionName = node.getImage();

        if(! (queryNode instanceof MetricIdNode)) {
            throw new MetrinkParseException("Alert queries can only contain one metric");
        }

        final AlertQuery alertQuery = alertQueryFactory.create(queryNode, triggerExpression, actionName);

        LOG.trace("Alert Query: {}", alertQuery);

        node.jjtSetValue(alertQuery);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTTriggerExpression node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final Comparator comparator = (Comparator) ((BaseNode)node.jjtGetChild(0)).jjtGetValue();
        final NumberArgument numberArgument = new NumberArgument((Number) ((BaseNode)node.jjtGetChild(1)).jjtGetValue());
        RelativeTimeArgument timeArgument = null;

        if(node.jjtGetNumChildren() == 3) {
            timeArgument = (RelativeTimeArgument) ((BaseNode)node.jjtGetChild(2)).jjtGetValue();
        }

        final TriggerExpression trigger = new TriggerExpression(comparator, numberArgument, timeArgument);

        LOG.trace("Trigger Expression: {}", trigger);

        node.jjtSetValue(trigger);

        return WalkType.POST_CHILDREN;
    }

    /*
     * We override this because we have restrictions on the types of metric functions that can be used.
     */
    @Override
    public WalkType visit(final ASTMetric node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final int numChildren = node.jjtGetNumChildren();

        if(numChildren < 3) {
            throw new MetrinkParseException("A metric must have 3 arguments (device, group, name), had " + numChildren);
        } else if(numChildren > 3) {
            throw new MetrinkParseException("A metric in an alert query cannot have an overlay time");
        }

        // convert everything into a String[]
        final String[] devices = node2StringArray((SimpleNode) node.jjtGetChild(0));
        final String[] groups  = node2StringArray((SimpleNode) node.jjtGetChild(1));
        final String[] names   = node2StringArray((SimpleNode) node.jjtGetChild(2));

        //
        // For now we're only accepting a single metric
        //
        if(devices.length != 1 ||
           groups.length != 1 ||
           names.length != 1) {
            throw new MetrinkParseException("Cannot specify more than one metric in an alert query");
        }

        int starCount = 0;

        // only allow 1 star for now
        if(devices[0].equals("*")) starCount++;
        if(groups[0].equals("*")) starCount++;
        if(names[0].equals("*")) starCount++;

        if(starCount > 1) {
            throw new MetrinkParseException("Cannot have a metric with more than one wildcard");
        }

        final MetricId id = new MetricId(devices[0], groups[0], names[0]);

        /* create the list of MetricIds
        final List<MetricId> metricIds = new ArrayList<MetricId>();

        for(String device:devices) {
            for(String group:groups) {
                for(String name:names) {
                    final MetricId id = new MetricId(owner, device, group, name);

                    LOG.trace("Metric id: {}", id);

                    metricIds.add(id);
                }
            }
        }
*/

        node.jjtSetValue(new MetricIdNode(id));

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTConnector node, final Boolean secondVisit) throws MetrinkParseException {
        throw new MetrinkParseException("Alert queries do not support connectors");
    }

    /*
     * We override this because we need to make sure times are NOT negative.
     */
    @Override
    public WalkType visit(final ASTRelativeTimeLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        if(secondVisit) {
            // call the super method to construct the RelativeTimeArgument in the node
            super.visit(node, secondVisit);

            final RelativeTimeArgument timeArg = (RelativeTimeArgument) node.jjtGetValue();

            if(timeArg.getDuration() < 0) {
                throw new MetrinkParseException("Relative times cannot be negative in alert queries.");
            }

            if(!timeArg.getTimeUnit().equals(TimeUnit.MINUTES)) {
                throw new MetrinkParseException("Only minutes are allowed for alert durations.");
            }

            if(timeArg.getDuration() >= 60) {
                throw new MetrinkParseException("Alert durations must be less than 60 minutes.");
            }
        }

        return WalkType.POST_CHILDREN;
    }

}
