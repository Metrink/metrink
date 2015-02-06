package com.metrink.grammar;

import com.metrink.grammar.javacc.MetrinkParserVisitor;
import com.metrink.grammar.javacc.Node;

public class ASTWalker {

    public MetrinkParserVisitor visitor;

    /**
     * Constructs a walker given a visitor
     * 
     * @param visitor
     */
    public ASTWalker(MetrinkParserVisitor visitor) {
        this.visitor = visitor;
    }

    public enum WalkType {
        PRE_CHILDREN, // just visit the node
        NO_CHILDREN, // don't visit the children, just return
        POST_CHILDREN // visit the node again after the children
    }

    /**
     * Given a node, calls accept on the node with the visitor, then walks the
     * children.
     * 
     * @param node
     *            The node to call accept on.
     */
    public void walk(Node node) throws MetrinkParseException {
        // call accept on the node indicating it's the pre-visit
        WalkType wt = node.jjtAccept(visitor, false);

        // we don't want to go down this branch
        if (wt == WalkType.NO_CHILDREN)
            return;

        // go through the children in order
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; ++i) {
            walk(node.jjtGetChild(i));
        }

        // if asked, visit the node again after the children
        if (wt == WalkType.POST_CHILDREN)
            node.jjtAccept(visitor, true);
    }

    /**
     * Given a node, calls accept on that node before calling accept on its
     * children
     * 
     * @param node
     * @throws MetrinkParseException
     */
    public void preorderWalk(Node node) throws MetrinkParseException {
        // call accept on the node indicating it's the pre-visit
        node.jjtAccept(visitor, false);

        // go through the children in order
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; ++i) {
            walk(node.jjtGetChild(i));
        }
    }

    /**
     * Given a node, calls accept on that node's children before calling accept
     * on the node.
     * 
     * @param node
     * @throws MetrinkParseException
     */
    public void postorderWalk(Node node) throws MetrinkParseException {
        // go through the children in order
        int numChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numChildren; ++i) {
            walk(node.jjtGetChild(i));
        }

        node.jjtAccept(visitor, true);
    }

}
