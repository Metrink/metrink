package com.metrink.grammar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.ASTWalker.WalkType;
import com.metrink.grammar.javacc.ASTAbsoluteDateLiteral;
import com.metrink.grammar.javacc.ASTAbsoluteTimeLiteral;
import com.metrink.grammar.javacc.ASTAdditiveExpression;
import com.metrink.grammar.javacc.ASTAlertQuery;
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
import com.metrink.grammar.javacc.ASTGraphQuery;
import com.metrink.grammar.javacc.ASTIntegerLiteral;
import com.metrink.grammar.javacc.ASTMetric;
import com.metrink.grammar.javacc.ASTMultiplicativeExpression;
import com.metrink.grammar.javacc.ASTNumberLiteral;
import com.metrink.grammar.javacc.ASTPercentLiteral;
import com.metrink.grammar.javacc.ASTRelativeTimeArray;
import com.metrink.grammar.javacc.ASTRelativeTimeIndicator;
import com.metrink.grammar.javacc.ASTRelativeTimeLiteral;
import com.metrink.grammar.javacc.ASTStringArray;
import com.metrink.grammar.javacc.ASTStringLiteral;
import com.metrink.grammar.javacc.ASTTriggerExpression;
import com.metrink.grammar.javacc.MetrinkParserVisitor;
import com.metrink.grammar.javacc.SimpleNode;

public abstract class AbstractASTVisitor implements MetrinkParserVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractASTVisitor.class);

    @Override
    public WalkType visit(SimpleNode node, Boolean secondVisit) throws MetrinkParseException {
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTCompilationUnit node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTAlertQuery node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTTriggerExpression node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTGraphQuery node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTGraphExpression node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTConnector node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTMetric node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTStringArray node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTFunction node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTFunctionName node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTArgumentList node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTArgument node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTAdditiveExpression node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTMultiplicativeExpression node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTStringLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTNumberLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTRelativeTimeArray node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTRelativeTimeLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTAbsoluteDateLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTAbsoluteTimeLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTPercentLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTBooleanLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTIntegerLiteral node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTRelativeTimeIndicator node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTConjunction node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

    @Override
    public WalkType visit(ASTComparator node, Boolean secondVisit) throws MetrinkParseException {
        LOG.warn("Visited non-overriden node {}", node.getClass().getCanonicalName());
        return WalkType.PRE_CHILDREN;
    }

}
