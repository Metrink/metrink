package com.metrink.gui.graphing;

import java.util.Map;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.graph.GraphObject;

public class DonutGraphPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DonutGraphPanel.class);

    private final GraphObject graphObject;
    private final RuntimeConfigurationType configType;
    private final String legendPosition;

    public DonutGraphPanel(final String id,
                           final Map<String, Double> values,
                           final String title,
                           final String centerValue,
                           final String legendPosition,
                           final String suffix) {
        super(id);

        this.setOutputMarkupPlaceholderTag(true);

        this.graphObject = new DonutGraphObject(values);
        this.configType = getApplication().getConfigurationType();
        this.legendPosition = legendPosition;

        add(new Label("title", title));
        add(new Label("center-value", centerValue));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        final String graphPanelId = this.getMarkupId(true);

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
            sb.append("', data, { legend: '");
            sb.append(legendPosition == null ? "none" : legendPosition);
            sb.append("' }); });");

            //LOG.debug("JSON: {}", graphObject.getJson());
            response.render(JavaScriptHeaderItem.forScript(sb.toString(), graphPanelId + "-js"));
        }
    }

}
