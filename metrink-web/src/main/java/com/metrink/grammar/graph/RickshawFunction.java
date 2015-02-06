package com.metrink.grammar.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.MetrinkParseException;
import com.metrink.gui.graphing.GraphPanel;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;

public class RickshawFunction extends GraphFunction {
    public static final Logger LOG = LoggerFactory.getLogger(RickshawFunction.class);

    @Override
    public GraphObject getGraphObject(final Map<MetricId, MetricValueList> searchResults) throws MetrinkParseException {
        return new LineGraphObject(searchResults);
    }

    /**
     * An object that contains all the needed code to render a graph.
     */
    public static class LineGraphObject implements GraphObject {
        private static final long serialVersionUID = 1L;

        private static final Logger LOG = LoggerFactory.getLogger(LineGraphObject.class);

        private static final CssResourceReference RICKSHAW_OVERRIDE_CSS_REF = new CssResourceReference(GraphPanel.class, "css/rickshaw-override.css");
        private static final CssResourceReference RICKSHAW_CSS_REF = new CssResourceReference(GraphPanel.class, "css/rickshaw.css");
        private static final CssResourceReference RICKSHAW_MIN_CSS_REF = new CssResourceReference(GraphPanel.class, "css/rickshaw.min.css");
        private static final JavaScriptResourceReference D3_MIN_REF = new JavaScriptResourceReference(GraphPanel.class, "js/d3.v3.min.js");
        private static final JavaScriptResourceReference D3_REF = new JavaScriptResourceReference(GraphPanel.class, "js/d3.v3.js");
        private static final JavaScriptResourceReference RICKSHAW_JS_REF = new JavaScriptResourceReference(GraphPanel.class, "js/rickshaw.js");
        private static final JavaScriptResourceReference RICKSHAW_MIN_JS_REF = new JavaScriptResourceReference(GraphPanel.class, "js/rickshaw.min.js");
        private static final JavaScriptResourceReference LINE_GRAPH_JS_REF = new JavaScriptResourceReference(GraphPanel.class, "js/line_graph.js");

        private final String json;

        public LineGraphObject(final Map<MetricId, MetricValueList> searchResults) {
            final JSONObject jsonObject = new JSONObject();
            final JSONArray labels = new JSONArray();
            final JSONArray metricsArray = new JSONArray();

            for(final Map.Entry<MetricId, MetricValueList> entry:searchResults.entrySet()) {
                final MetricValueList metricValues = entry.getValue();

                LOG.debug("{} values for {}", metricValues.size(), entry.getKey().toString());

                if(metricValues.size() == 0) {
                    LOG.warn("Found no data for {}", entry.getKey().toString());
                    labels.put(entry.getKey());
                    metricsArray.put(new JSONArray());
                } else {
                    labels.put(entry.getKey());

                    metricsArray.put(collectionToJsonArray(metricValues.getValues()));
                }
            }

            try {
                jsonObject.put("labels", labels);
                jsonObject.put("metrics", metricsArray);
            } catch (final JSONException e) {
                LOG.error("Error generating JSON: {}", e.getMessage());
            }
            json = jsonObject.toString();
        }

        private JSONArray collectionToJsonArray(final Collection<MetricValue> metricValues) {
            final JSONArray ret = new JSONArray();

            for(final MetricValue value:metricValues) {
                try {
                    ret.put(new JSONObject().put("x", value.getTimestamp()/1000).put("y", value.getValue()));
                } catch(final JSONException e) {
                    LOG.error("Error converting to JSON: {}", e.getMessage());
                }
            }

            return ret;
        }

        @Override
        public String getJson(final RuntimeConfigurationType runtimeType) {
            return json;
        }

        @Override
        public String getJavaScriptFunction(final RuntimeConfigurationType runtimeType) {
            return "line_graph";
        }

        @Override
        public List<CssReferenceHeaderItem> getCssHeaderItems(final RuntimeConfigurationType runtimeType) {
            if(runtimeType.equals(RuntimeConfigurationType.DEVELOPMENT)) {
                return Arrays.asList(
                        CssHeaderItem.forReference(RICKSHAW_CSS_REF),
                        CssHeaderItem.forReference(RICKSHAW_OVERRIDE_CSS_REF));
            } else {
                return Arrays.asList(
                        CssHeaderItem.forReference(RICKSHAW_MIN_CSS_REF),
                        CssHeaderItem.forReference(RICKSHAW_OVERRIDE_CSS_REF));
            }
        }

        @Override
        public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(final RuntimeConfigurationType runtimeType) {
            if(runtimeType.equals(RuntimeConfigurationType.DEVELOPMENT)) {
                return Arrays.asList(JavaScriptHeaderItem.forReference(D3_REF),
                                     JavaScriptHeaderItem.forReference(RICKSHAW_JS_REF),
                                     JavaScriptHeaderItem.forReference(LINE_GRAPH_JS_REF));
            } else {
                return Arrays.asList(JavaScriptHeaderItem.forReference(D3_MIN_REF),
                                     JavaScriptHeaderItem.forReference(RICKSHAW_MIN_JS_REF),
                                     JavaScriptHeaderItem.forReference(LINE_GRAPH_JS_REF));
            }
        }
    }
}