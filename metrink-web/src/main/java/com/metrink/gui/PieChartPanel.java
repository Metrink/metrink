package com.metrink.gui;

import java.util.Map;

import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PieChartPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(PieChartPanel.class);

    public PieChartPanel(final String id, final Map<String, Long> map) {
        super(id);

        add(new Label("graph-data", mapToJavaScript(map)).setEscapeModelStrings(false));
    }

    protected String mapToJavaScript(final Map<String, Long> values) {
        StringBuffer sb = new StringBuffer("var dataset = ");

        sb.append(mapToJSON(values).toString());
        sb.append(";\n");
        sb.append("var centerVal = '");
        sb.append(sumValues(values));
        sb.append("';\n");

        return sb.toString();
    }

    protected Long sumValues(final Map<String, Long> values) {
        Long ret = Long.valueOf(0);

        for(Map.Entry<String, Long> v:values.entrySet()) {
            ret += v.getValue();
        }

        return ret;
    }

    protected JSONArray mapToJSON(final Map<String, Long> values) {
        final JSONArray ret = new JSONArray();

        if(values.size() == 0) {
            return new JSONArray();
        }

        try {
            for(Map.Entry<String, Long> v:values.entrySet()) {
                final JSONObject kvObj = new JSONObject();

                kvObj.put("k", v.getKey());
                kvObj.put("v", v.getValue());

                ret.put(kvObj);
            }

            return ret;
        } catch(JSONException e) {
            LOG.error("Error creating metric JSON: {}", e.getMessage());
            return new JSONArray();
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(new UrlResourceReference(Url.parse("http://d3js.org/d3.v3.min.js"))));

        //response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PieChartPanel.class, "nv.d3.js")));
        //response.render(CssHeaderItem.forReference(new CssResourceReference(PieChartPanel.class, "nv.d3.css")));
    }

}
