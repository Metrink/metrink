package com.metrink.gui.graphing;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.metric.Metric;

public class GaugePanel extends Panel {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GaugePanel.class);

    public GaugePanel(final String id, final Metric[] metrics) {
        super(id);

        if(metrics.length == 0) {
            add(new Label("gauge-data", ""));
        } else {
            add(new Label("gauge-data", metricsToJavaScript(metrics)).setEscapeModelStrings(false));
        }
    }

    protected String metricsToJavaScript(final Metric[] metrics) {
        final StringBuffer sb = new StringBuffer("var score = [");

        // TODO: get this working!!!
        // sb.append(metricsToJSON(metrics).toString());

        sb.append("3 ];");

        return sb.toString();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(new UrlResourceReference(Url.parse("http://d3js.org/d3.v2.min.js"))));

        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(GaugePanel.class, "js/nv.d3.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(GaugePanel.class, "css/nv.d3.css")));
    }

}
