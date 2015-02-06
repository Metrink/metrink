package com.metrink.gui.graphing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.graph.GraphObject;

public class CandlestickGraphObject implements GraphObject {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CandlestickGraphObject.class);

    private static final UrlResourceReference GOOGLE_REF = new UrlResourceReference(Url.parse("https://www.google.com/jsapi"));
    private static final JavaScriptResourceReference CANDLESTICK_JS_REF = new JavaScriptResourceReference(GraphPanel.class, "js/candlestick_graph.js");

    private final String json;

    /**
     * Creates a candlestick graph. The Map should contain labels and then the values
     * are shown with min, 25%, 75%, and max.
     */
    public CandlestickGraphObject(final Map<String, Iterator<Double>> values) {
        final JSONArray dataArray = new JSONArray();

        try {
            // parse the map and create the JSON
            for(Map.Entry<String, Iterator<Double>> value:values.entrySet()) {
                final JSONArray row = new JSONArray();

                row.put(value.getKey());

                final double[] stats = computeStats(value.getValue());

                for(double s:stats) {
                    row.put(s);
                }

                dataArray.put(row);
            }
        } catch (JSONException e) {
            LOG.error("Error creating JSON: {}", e.getMessage());
        }

        json = dataArray.toString();
    }

    protected double[] computeStats(final Iterator<Double> it) {
        final DescriptiveStatistics stats = new DescriptiveStatistics();

        while(it.hasNext()) {
            stats.addValue(it.next());
        }

        return new double[] { stats.getMin(), stats.getPercentile(25), stats.getPercentile(75), stats.getMax() };
    }

    @Override
    public String getJson(RuntimeConfigurationType runtimeType) {
        return json;
    }

    @Override
    public String getJavaScriptFunction(RuntimeConfigurationType runtimeType) {
        return "draw_candlestick_chart";
    }

    @Override
    public List<CssReferenceHeaderItem> getCssHeaderItems(RuntimeConfigurationType runtimeType) {
        return Arrays.asList();
    }

    @Override
    public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(RuntimeConfigurationType runtimeType) {
        return Arrays.asList(JavaScriptHeaderItem.forReference(GOOGLE_REF), JavaScriptHeaderItem.forReference(CANDLESTICK_JS_REF));
    }

}
