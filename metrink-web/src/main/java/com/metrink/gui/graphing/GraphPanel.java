package com.metrink.gui.graphing;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.graph.GraphObject;

public class GraphPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(GraphPanel.class);

    private final GraphObject graphObject;
    private final RuntimeConfigurationType configType;
    private String graphPanelId;
    private Integer height;

    public GraphPanel(final String id, final GraphObject graphObject) {
        this(id, graphObject, "");
    }

    public GraphPanel(final String id, final GraphObject graphObject, final String suffix) {
        super(id);

        this.graphObject = graphObject;
        this.configType = getApplication().getConfigurationType();

        final WebMarkupContainer graphDiv = new WebMarkupContainer("graph-div");

        graphPanelId = "graph-panel-" + id + suffix;
        graphDiv.add(new AttributeModifier("id", graphPanelId));

        add(graphDiv);

        LOG.debug("Graph Object: {}", graphObject);
    }

    public GraphPanel setHeight(final Integer height) {
        this.height = height;
        return this;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // HACK: Force the inclusion of Wicket's jQuery. This is definitely the wrong thing to do, because StiLearn
        // ships with it's own version of jQuery which we minify.
        response.render(JavaScriptHeaderItem.forReference(
            getApplication()
                .getJavaScriptLibrarySettings()
                .getJQueryReference()));

        if(graphObject != null) {
            for(final CssReferenceHeaderItem item:graphObject.getCssHeaderItems(configType)){
                response.render(item);
            }

            for(final JavaScriptHeaderItem item:graphObject.getJavaScriptHeaderItem(configType)) {
                response.render(item);
            }

            final StringBuilder sb = new StringBuilder("$(function() { var data = ");

            sb.append(graphObject.getJson(configType));
            sb.append("; ");
            sb.append(graphObject.getJavaScriptFunction(configType));
            sb.append("('#");
            sb.append(graphPanelId);
            sb.append("', data, { height: ");
            sb.append(height == null ? "null" : height);
            sb.append(" }); });");

            //LOG.debug("JSON: {}", graphObject.getJson());
            response.render(JavaScriptHeaderItem.forScript(sb.toString(), graphPanelId + "-js"));
        }
    }

}
