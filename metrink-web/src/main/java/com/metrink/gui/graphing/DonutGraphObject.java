package com.metrink.gui.graphing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.graph.GraphObject;

public class DonutGraphObject implements GraphObject {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DonutGraphObject.class);

    private static final UrlResourceReference GOOGLE_REF = new UrlResourceReference(Url.parse("https://www.google.com/jsapi"));
    private static final CssResourceReference DONUT_CSS_REF = new CssResourceReference(GraphPanel.class, "css/donut_graph.css");
    private static final JavaScriptResourceReference DONUT_JS_REF = new JavaScriptResourceReference(GraphPanel.class, "js/donut_graph.js");

    private final String json;

    public DonutGraphObject(final Map<String, Double> values) {
        final JSONArray dataArray = new JSONArray();

        final JSONArray labels = new JSONArray();

        labels.put("Labels");
        labels.put("Values");

        dataArray.put(labels);

        if(values.isEmpty()) {
            LOG.warn("Empty donut chart");
        }

        // parse the map and create the JSON
        for(Map.Entry<String, Double> value:values.entrySet()) {
            final JSONArray row = new JSONArray();

            row.put(value.getKey());
            row.put(value.getValue());

            dataArray.put(row);
        }

        json = dataArray.toString();
    }

    @Override
    public String getJson(RuntimeConfigurationType runtimeType) {
        return json;
    }

    @Override
    public String getJavaScriptFunction(RuntimeConfigurationType runtimeType) {
        return "donut_graph";
    }

    @Override
    public List<CssReferenceHeaderItem> getCssHeaderItems(RuntimeConfigurationType runtimeType) {
        return Arrays.asList(CssHeaderItem.forReference(DONUT_CSS_REF));
    }

    @Override
    public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(RuntimeConfigurationType runtimeType) {
        return Arrays.asList(JavaScriptHeaderItem.forReference(GOOGLE_REF), JavaScriptHeaderItem.forReference(DONUT_JS_REF));
    }
}
