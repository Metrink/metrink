package com.metrink.grammar.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.query.QueryFunction;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricReader;
import com.metrink.utils.MilliSecondUtils;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class KurtosisFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(KurtosisFunction.class);

    private int WINDOW_SIZE = 10; // 10 minutes

    private final MetricReader reader;

    @Inject
    public KurtosisFunction(final MetricReader reader) {
        this.reader = reader;
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {

        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(final Map.Entry<MetricId, MetricValueList> entry:context.entrySet()) {

            //
            // Update this to take an arg for the window size
            //
            final long newStart = start + MilliSecondUtils.minutesToMs(WINDOW_SIZE/2) * -1;

            // go back WINDOW_SIZE/2 for the current values
            final MetricValueList currentValues = new MetricValueList(newStart, entry.getValue().getEndTime());

            currentValues.addMetricValues(reader.readMetrics(entry.getKey(), newStart, start));
            currentValues.addMetricValues(entry.getValue());

            final DescriptiveStatistics currentStats = new DescriptiveStatistics(Arrays.copyOf(currentValues.getDoubleValues(), WINDOW_SIZE));
            currentStats.setWindowSize(WINDOW_SIZE);

            final List<MetricValue> origValueList = currentValues.getValues();
            final List<MetricValue> kurtValues = new ArrayList<>();
            final List<MetricValue> varValues = new ArrayList<>();

            for(int i=WINDOW_SIZE/2; i < origValueList.size()-(WINDOW_SIZE/2)-1; ++i) {
                kurtValues.add(new MetricValue(origValueList.get(i)).setValue(currentStats.getKurtosis()));
                varValues.add(new MetricValue(origValueList.get(i)).setValue(currentStats.getVariance()));

                currentStats.addValue(origValueList.get(i+(WINDOW_SIZE/2)+1).getValue());
            }
            final MetricId kurtosisMetricId = new MetricId(entry.getKey());
            final MetricId varianceMetricId = new MetricId(entry.getKey());

            kurtosisMetricId.setName("KURTOSIS");
            varianceMetricId.setName("VARIANCE");

            ret.put(kurtosisMetricId, new MetricValueList(kurtValues));
            ret.put(varianceMetricId, new MetricValueList(varValues));
        }

        return ret;
    }
}
