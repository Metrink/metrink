package com.metrink.grammar.graph;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;

/**
 * Representation of a graph.
 *
 * Provides all CSS, Javascript, and JSON needed to render the graph.
 */
public interface GraphObject extends Serializable {

    /**
     * Returns the JSON representing the graph.
     * @param runtimeType the runtime of the system: deployment or development.
     * @return JSON representing the graph.
     */
    public String getJson(final RuntimeConfigurationType runtimeType);

    /**
     * Get the JavaScript function to call for this graph.
     * @param runtimeType the runtime of the system: deployment or development.
     * @return name of the JavaScript function
     */
    public String getJavaScriptFunction(RuntimeConfigurationType runtimeType);

    /**
     * Provides the {@link CssReferenceHeaderItem}s needed to render the graph.
     * @param runtimeType the runtime of the system: deployment or development.
     * @return list of {@link CssReferenceHeaderItem}s.
     */
    public List<CssReferenceHeaderItem> getCssHeaderItems(final RuntimeConfigurationType runtimeType);

    /**
     * Provides the {@link JavaScriptReferenceHeaderItem}s needed to render the graph.
     * @param runtimeType the runtime of the system: deployment or development.
     * @return list of {@link JavaScriptReferenceHeaderItem}s.
     */
    public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(final RuntimeConfigurationType runtimeType);
}
