package com.metrink.grammar.graph;

import java.util.List;
import java.util.Map;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.metrink.gui.graphing.GraphPanel;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValueList;
import com.google.common.collect.Lists;

public class AreaGraphFunction extends LineGraphFunction {

    @Override
    public LineGraphObject getGraphObject(final Map<MetricId, MetricValueList> searchResults) {
        return new AreaGraphObject(searchResults);
    }


    public static class AreaGraphObject extends LineGraphObject {
        private static final long serialVersionUID = 1L;
        private static JavaScriptResourceReference graphObjectJavascriptReference
                = new JavaScriptResourceReference(GraphPanel.class, "js/area_graph.js");

        public AreaGraphObject(final Map<MetricId, MetricValueList> searchResults) {
            super(searchResults);
        }

        @Override
        public String getJavaScriptFunction(final RuntimeConfigurationType runtimeType) {
            return "area_graph";
        }

        @Override
        public List<CssReferenceHeaderItem> getCssHeaderItems(final RuntimeConfigurationType runtimeType) {
            return super.getCssHeaderItems(runtimeType);
        }

        @Override
        public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(final RuntimeConfigurationType runtimeType) {
            final List<JavaScriptReferenceHeaderItem> javaScriptHeaderItems
                    = Lists.newArrayList(super.getJavaScriptHeaderItem(runtimeType));
            javaScriptHeaderItems.add(JavaScriptHeaderItem.forReference(graphObjectJavascriptReference));
            return javaScriptHeaderItems;
        }
    }
}
