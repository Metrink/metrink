package com.metrink.grammar.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the basics of assigning and return children.
 */
public abstract class AbstractQueryNode implements QueryNode {
    public static final Logger LOG = LoggerFactory.getLogger(AbstractQueryNode.class);

    public final QueryNode leftChild;
    public final QueryNode rightChild;

    /**
     * Constructor for terminal nodes.
     */
    public AbstractQueryNode() {
        this.leftChild = this.rightChild = null;
    }

    /**
     * Constructor for intermediary nodes.
     * @param leftChild the left child in the tree.
     * @param rightChild the right child in the tree.
     */
    public AbstractQueryNode(final QueryNode leftChild, final QueryNode rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public QueryNode getLeftChild() {
        return leftChild;
    }

    @Override
    public QueryNode getRightChild() {
        return rightChild;
    }

}
