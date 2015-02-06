package com.metrink.grammar.graph;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class HistogramFunction extends GraphFunction {

    private static final Logger LOG = LoggerFactory.getLogger(HistogramFunction.class);

    @Override
    public GraphObject getGraphObject(final Map<MetricId, MetricValueList> searchResults)
            throws MetrinkParseException {
        final JSONArray labels = new JSONArray();
        final JSONArray metricsArray = new JSONArray();

        final int buckets = 20;
        final SummaryStatistics statistics = getSummaryStatistics(searchResults);

        final double range = statistics.getMax() - statistics.getMin();

        // expanding the range just beyond the bounds to hopefully avoid floating point errors
        final double bucketSize = range / buckets;
        final double start = statistics.getMin();

        LOG.debug("Creating histogram of range {} with bucket size of {} and a start of {}", range, bucketSize, start);

        final JSONObject jsonBuckets = new JSONObject();
        try {
            for (final Entry<MetricId, MetricValueList> resultEntry : searchResults.entrySet()) {
                final JSONArray metricArray = new JSONArray();

                final Multiset<Double> set = bucketMetrics(resultEntry, bucketSize, start);


                for (final com.google.common.collect.Multiset.Entry<Double> result : set.entrySet()) {
                    LOG.debug("Bucket {} contains {} entries", result.getElement(), result.getCount());

                    final JSONObject jsonBucket = new JSONObject();
                    jsonBucket.put("x", result.getElement());
                    jsonBucket.put("y", result.getCount());
                    jsonBucket.put("dx", bucketSize);
                    metricArray.put(jsonBucket);
                }

                metricsArray.put(metricArray);
            }

            // TODO: In this context, adding labels makes no sense. I'm persisting this logic for stacked histograms.
            for (final Map.Entry<MetricId, MetricValueList> entry:searchResults.entrySet()) {
                final MetricValueList metricValues = entry.getValue();

                LOG.debug("{} values for {}", metricValues.size(), entry.getKey());
                labels.put(entry.getKey());

                if(metricValues.size() == 0) {
                    LOG.warn("Found no data for {}", entry.getKey());
                }
            }

            jsonBuckets.put("labels", labels);
            jsonBuckets.put("metrics", metricsArray);

        } catch (final JSONException e) {
            LOG.error("Error generating JSON: {}", e.getMessage());
        }

        return new HistogramGraphObject(jsonBuckets);
    }

    private Multiset<Double> bucketMetrics(
            final Entry<MetricId, MetricValueList> resultEntry,
            final double bucketWidth,
            final double start) throws MetrinkParseException {

        final Multiset<Double> bucketed = TreeMultiset.create();

        for (final MetricValue value : resultEntry.getValue()) {

            final int bucketIndex = (int)((value.getValue() - start) / bucketWidth);
            final double bucketValue = bucketIndex * bucketWidth + start;

            bucketed.add(bucketValue);
            LOG.trace("Bucketing {} into {}", value.getValue(), bucketValue);
        }

        return bucketed;
    }

    private SummaryStatistics getSummaryStatistics(final Map<MetricId, MetricValueList> searchResults) {
        final SummaryStatistics statistics = new SummaryStatistics();
        for (final Entry<MetricId, MetricValueList> resultEntry : searchResults.entrySet()) {
            for (final MetricValue value : resultEntry.getValue()) {
                statistics.addValue(value.getValue());
            }
        }
        return statistics;
    }

    public static class HistogramGraphObject implements GraphObject {
        private static final long serialVersionUID = 1L;

        private static final List<CssReferenceHeaderItem> CSS_RESOURCES = Arrays.asList(
                CssHeaderItem.forReference(new CssResourceReference(GraphPanel.class, "css/histogram.css"))
                );

        private static final List<JavaScriptReferenceHeaderItem> JAVASCRIPT_RESOURCES = Arrays.asList(
                JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(GraphPanel.class, "js/d3.v3.min.js")),
                JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(GraphPanel.class, "js/histogram.js"))
                );
        private static final List<JavaScriptReferenceHeaderItem> JAVASCRIPT_RESOURCES_DEVELOPMENT = Arrays.asList(
                JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(GraphPanel.class, "js/d3.v3.js")),
                JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(GraphPanel.class, "js/histogram.js"))
                );

        private final String json;

        public HistogramGraphObject(final JSONObject jsonObject) {
            this.json = jsonObject.toString();
        }

        @Override
        public String getJson(final RuntimeConfigurationType runtimeType) {
            return json;
        }

        @Override
        public String getJavaScriptFunction(final RuntimeConfigurationType runtimeType) {
            return "multi_line_graph";
        }

        @Override
        public List<CssReferenceHeaderItem> getCssHeaderItems(final RuntimeConfigurationType runtimeType) {
            return CSS_RESOURCES;
        }

        @Override
        public List<JavaScriptReferenceHeaderItem> getJavaScriptHeaderItem(final RuntimeConfigurationType runtimeType) {
            return runtimeType.equals(RuntimeConfigurationType.DEVELOPMENT)
                ? JAVASCRIPT_RESOURCES_DEVELOPMENT
                : JAVASCRIPT_RESOURCES;
        }
    }
}
