package com.metrink.grammar;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.ASTWalker.WalkType;
import com.metrink.grammar.javacc.ASTAbsoluteDateLiteral;
import com.metrink.grammar.javacc.ASTAbsoluteTimeLiteral;
import com.metrink.grammar.javacc.ASTAdditiveExpression;
import com.metrink.grammar.javacc.ASTArgument;
import com.metrink.grammar.javacc.ASTArgumentList;
import com.metrink.grammar.javacc.ASTBooleanLiteral;
import com.metrink.grammar.javacc.ASTComparator;
import com.metrink.grammar.javacc.ASTCompilationUnit;
import com.metrink.grammar.javacc.ASTConjunction;
import com.metrink.grammar.javacc.ASTConnector;
import com.metrink.grammar.javacc.ASTFunction;
import com.metrink.grammar.javacc.ASTFunctionName;
import com.metrink.grammar.javacc.ASTGraphExpression;
import com.metrink.grammar.javacc.ASTIntegerLiteral;
import com.metrink.grammar.javacc.ASTMultiplicativeExpression;
import com.metrink.grammar.javacc.ASTNumberLiteral;
import com.metrink.grammar.javacc.ASTPercentLiteral;
import com.metrink.grammar.javacc.ASTRelativeTimeArray;
import com.metrink.grammar.javacc.ASTRelativeTimeIndicator;
import com.metrink.grammar.javacc.ASTRelativeTimeLiteral;
import com.metrink.grammar.javacc.ASTStringArray;
import com.metrink.grammar.javacc.ASTStringLiteral;
import com.metrink.grammar.javacc.SimpleNode;
import com.metrink.grammar.query.ConnectorNode;
import com.metrink.grammar.query.ConstantMetricNode;
import com.metrink.grammar.query.MathFunction;
import com.metrink.grammar.query.QueryFunction;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.grammar.query.QueryNode;

/**
 * An AST Visitor that provides the ability to walk the common components of a Query.
 */
public abstract class BaseQueryVisitor extends AbstractASTVisitor {

    private final static Logger LOG = LoggerFactory.getLogger(BaseQueryVisitor.class);

    protected Query query;
    protected final QueryFunctionFactory queryFunctionFactory;

    // for injection only
    public BaseQueryVisitor() {
        this.query = null;
        this.queryFunctionFactory = null;
    }

    public BaseQueryVisitor(final QueryFunctionFactory queryFunctionFactory) {
        this.queryFunctionFactory = queryFunctionFactory;
    }

    /**
     * Gets the resulting {@link Query} from parsing the string.
     * @return the parsed Query object.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Returns the time zone to use to interpret all inputs.
     * @return the time zone to use to interpret all inputs.
     */
    protected abstract DateTimeZone getTimeZone();

    @Override
    public WalkType visit(final ASTCompilationUnit node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        // get the query
        query = (Query)((BaseNode)node.jjtGetChild(0)).jjtGetValue();

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTGraphExpression node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        // if we only have one, then flatten the tree
        if(node.jjtGetNumChildren() == 1) {
            node.jjtSetValue(((SimpleNode) node.jjtGetChild(0)).jjtGetValue());
            return WalkType.POST_CHILDREN;
        }

        int i=0;

        // find the last AdditiveExpression in our search
        for(; i < node.jjtGetNumChildren(); ++i) {
            if(! (node.jjtGetChild(i) instanceof ASTAdditiveExpression)) {
                break;
            }
        }

        QueryNode prev = (QueryNode) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();

        // start building up our tree
        int j=1;
        for( ; j < i; ++j) {
            final QueryNode thisNode = (QueryNode) ((SimpleNode) node.jjtGetChild(j)).jjtGetValue();

            prev = new ConnectorNode(prev, thisNode, ConnectorNode.Type.COPY);
        }

        // continue building the tree with the specified connector
        for( ; j < node.jjtGetNumChildren(); j += 2) {
            final String type = (String) ((SimpleNode) node.jjtGetChild(j)).jjtGetValue();
            final QueryNode thisNode = (QueryNode) ((SimpleNode) node.jjtGetChild(j+1)).jjtGetValue();

            prev = new ConnectorNode(prev, thisNode, type);
        }

        node.jjtSetValue(prev);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTConnector node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final String image = ((BaseNode)node).getImage();

        LOG.trace("Connector: {}", image);

        node.jjtSetValue(image);

        return WalkType.POST_CHILDREN;
    }

    protected String[] node2StringArray(SimpleNode node) throws MetrinkParseException {

        final Object obj = node.jjtGetValue();
        String[] ret;

        if(obj instanceof String[]) {
            ret = (String[]) obj;
        } else if(obj instanceof String) {
            ret = new String[]{ ((String)obj).trim() };
        } else {
            throw new MetrinkParseException("Error not a string: " + obj);
        }

        // we need to make sure that there isn't a * mixed with specific literals
        if(ret.length > 1) {
            for(String r:ret) {
                if(r.equals("*")) {
                    throw new MetrinkParseException("Cannot mix literals and wildcards");
                }
            }
        }

        return ret;
    }

    @Override
    public WalkType visit(final ASTStringArray node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final String[] values = new String[node.jjtGetNumChildren()];

        for(int i=0; i < node.jjtGetNumChildren(); ++i) {
            values[i] = ((String) ((SimpleNode) node.jjtGetChild(i)).jjtGetValue()).trim();
        }

        node.jjtSetValue(values);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTFunction node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        QueryFunction function = null;

        final String name = (String) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();

        // will have 1 or 2 children
        if(node.jjtGetNumChildren() == 1) {
            function = queryFunctionFactory.create(name);
        } else if(node.jjtGetNumChildren() == 2) {
            @SuppressWarnings("unchecked")
            final List<Argument> args = (List<Argument>) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();
            function = queryFunctionFactory.create(name, args);
        } else {
            throw new MetrinkParseException("Wrong number of children for Function: " + node.jjtGetNumChildren());
        }

        node.jjtSetValue(function);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTFunctionName node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Function: {}", image);

        node.jjtSetValue(image);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTArgumentList node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final List<Argument> args = new ArrayList<Argument>();

        for(int i=0; i < node.jjtGetNumChildren(); ++i) {
            args.add((Argument) ((SimpleNode) node.jjtGetChild(i)).jjtGetValue());
        }

        LOG.trace("Argument list with {} arguments", args.size());

        node.jjtSetValue(args);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTArgument node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        Argument arg = null;

        if(sn instanceof ASTStringLiteral) {
            arg = new StringArgument((String) sn.jjtGetValue());
        } else if(sn instanceof ASTRelativeTimeLiteral) {
            arg = (Argument) sn.jjtGetValue();
        } else if(sn instanceof ASTPercentLiteral) {
            arg = (Argument) sn.jjtGetValue();
        } else if(sn instanceof ASTNumberLiteral) {
            arg = new NumberArgument((Number) sn.jjtGetValue());
        } else if(sn instanceof ASTBooleanLiteral) {
            arg = new BooleanArgument((Boolean) sn.jjtGetValue());
        } else {
            throw new MetrinkParseException("Unknown node type: " + sn.getClass().getCanonicalName());
        }

        LOG.trace("Argument: {}", arg);

        node.jjtSetValue(arg);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTAdditiveExpression node, final Boolean secondVisit) throws MetrinkParseException {
        return handleMathOperation(node, secondVisit);
    }

    @Override
    public WalkType visit(final ASTMultiplicativeExpression node, final Boolean secondVisit) throws MetrinkParseException {
        return handleMathOperation(node, secondVisit);
    }

    private WalkType handleMathOperation(final BaseNode node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        Object childValue = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();

        // with one child, just flatten the tree
        if(node.jjtGetNumChildren() == 1) {
            node.jjtSetValue(childValue);
            return WalkType.POST_CHILDREN;
        }

        QueryNode leftChild;

        // figure out what type the leftChild is
        if(childValue instanceof Double) {
            leftChild = new ConstantMetricNode((Double) childValue);
        } else if(childValue instanceof Long) {
            leftChild = new ConstantMetricNode((Long) childValue);
        } else {
            leftChild = (QueryNode) childValue;
        }

        QueryNode rightChild;

        childValue = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();

        // figure out what the rightChild is
        if(childValue instanceof Double) {
            rightChild = new ConstantMetricNode((Double) childValue);
        } else if(childValue instanceof Long) {
            rightChild = new ConstantMetricNode((Long) childValue);
        } else {
            rightChild = (QueryNode) childValue;
        }

        // do a few sanity checks on the operation
        if(node.getImage().length() != 1) {
            throw new MetrinkParseException("Unknown math operation: " + node.getImage());
        }

        final char op = node.getImage().charAt(0);

        if(op != '+' && op != '-' && op != '*' && op != '/') {
            throw new MetrinkParseException("Unknown math operation: " + node.getImage());
        }

        node.jjtSetValue(new MathFunction(leftChild, rightChild, op));

        return WalkType.POST_CHILDREN;
    }


    @Override
    public WalkType visit(final ASTStringLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("String: {}", image);

        node.jjtSetValue(image.replaceAll("^['\"]", "").replaceAll("['\"]$", ""));

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTNumberLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Number: {}", image);

        if(image.contains(".")) {
            node.jjtSetValue(Double.parseDouble(image));
        } else {
            node.jjtSetValue(Long.parseLong(image));
        }

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTRelativeTimeArray node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final List<Argument> args = new ArrayList<>(node.jjtGetNumChildren());

        for(int i=0; i < node.jjtGetNumChildren(); ++i) {
            args.add((Argument) ((BaseNode)node.jjtGetChild(i)).jjtGetValue());
        }

        LOG.trace("Relative Time Array: {}", args.size());

        node.jjtSetValue(args);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTRelativeTimeLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final String unitArg = ((BaseNode)node.jjtGetChild(1)).getImage();

        Long duration = Long.parseLong(((BaseNode)node.jjtGetChild(0)).getImage());

        if(duration == 0) {
            throw new MetrinkParseException("Duration cannot be zero");
        }

        if(node.getImage() != null && node.getImage().equals("-")) {
            duration *= -1;
        }

        final RelativeTimeArgument timeArg = new RelativeTimeArgument(duration, unitArg);

        LOG.trace("Time : {}", timeArg);

        node.jjtSetValue(timeArg);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTAbsoluteDateLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        final Long year = (Long) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();
        final Long month = (Long) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();
        final Long day = (Long) ((SimpleNode) node.jjtGetChild(2)).jjtGetValue();
        final LocalTime time = (LocalTime) ((SimpleNode) node.jjtGetChild(3)).jjtGetValue();

        // check the year, month, and day
        try {
            checkArgument(year >= 1900 & year < 3000, "Invalid year: %s", year);
            checkArgument(month >= 1 & month <= 12, "Invalid month: %s", month);
            checkArgument(day >= 1 & day <= 31, "Invalid day: %s", day);
        } catch(IllegalArgumentException e) {
            throw new MetrinkParseException(e);
        }

        // create a DateTime using the correct timezone
        final DateTime absDateTime = new DateTime(year.intValue(),
                                                  month.intValue(),
                                                  day.intValue(),
                                                  time.getHourOfDay(),
                                                  time.getMinuteOfHour(),
                                                  getTimeZone());

        node.jjtSetValue(absDateTime);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTAbsoluteTimeLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        if(!secondVisit) {
            return WalkType.POST_CHILDREN;
        }

        Long hours = (Long) ((SimpleNode) node.jjtGetChild(0)).jjtGetValue();
        final Long minutes = (Long) ((SimpleNode) node.jjtGetChild(1)).jjtGetValue();

        // check the hours & minutes
        try {
            checkArgument(hours >= 0 & hours < 24, "Invalid hour: %s", hours);
            checkArgument(minutes >= 0 & minutes < 60, "Invalid minute: %s", hours);
        } catch(IllegalArgumentException e) {
            throw new MetrinkParseException(e);
        }

        final String image = node.getImage();

        // check if we have AM or PM: HH:MM[am/pm]
        if(image != null) {
            if(image.equalsIgnoreCase("am") && hours == 12) {
                hours = 0l;
            } else if(image.equalsIgnoreCase("pm") && hours < 12) {
                hours += 12;
            }
        }

        final LocalTime time = new LocalTime(hours.intValue(), minutes.intValue());

        node.jjtSetValue(time);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTPercentLiteral node, final Boolean secondVisit) throws MetrinkParseException {

        Argument arg = null;

        if(node.jjtGetNumChildren() == 1) {
            final Integer percent = Integer.parseInt(((BaseNode)node.jjtGetChild(0)).getImage());
            arg = new PercentArgument(percent);
        } else if(node.jjtGetNumChildren() == 2) {
            final boolean isNegative = ((BaseNode)node.jjtGetChild(0)).getImage().equals("-");
            final Integer percent = Integer.parseInt(((BaseNode)node.jjtGetChild(1)).getImage());

            arg = new PercentArgument(isNegative ? percent * -1 : percent);
        } else {
            throw new MetrinkParseException("Unknown number of children for Percent Literal: " + node.jjtGetNumChildren());
        }


        LOG.trace("Percent: {}", arg);

        node.jjtSetValue(arg);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTBooleanLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Boolean: {}", image);

        node.jjtSetValue(Boolean.parseBoolean(image));

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTIntegerLiteral node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Integer: {}", image);

        node.jjtSetValue(Long.parseLong(image));

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTRelativeTimeIndicator node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Time Indicator: {}", image);

        node.jjtSetValue(image);

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTConjunction node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Conjunction: {}", image);

        node.jjtSetValue(new Conjunction(image));

        return WalkType.POST_CHILDREN;
    }

    @Override
    public WalkType visit(final ASTComparator node, final Boolean secondVisit) throws MetrinkParseException {
        final String image = ((BaseNode)node).getImage();

        LOG.trace("Comparator: {}", image);

        node.jjtSetValue(new Comparator(image));

        return WalkType.POST_CHILDREN;
    }
}
