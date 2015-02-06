package com.metrink.grammar.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.DisplayMetricId;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.MetricValue.GetTimestampFunction;
import com.metrink.metric.io.MetricReader;
import com.metrink.utils.MilliSecondUtils;


public class PredictFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(PredictFunction.class);

    private static final long ONE_DAY_OFFSET = TimeUnit.DAYS.toMillis(1) * -1;
    private static final long SEVEN_DAYS_OFFSET = TimeUnit.DAYS.toMillis(7) * -1;

    private static final int WINDOW_SIZE = 10; // 10 minutes

    private final MetricReader reader;

    @Inject
    public PredictFunction(final MetricReader reader) {
        this.reader = reader;
    }

    @Override
    public Map<MetricId, MetricValueList> process(long start, long end, ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {

        Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(Map.Entry<MetricId, MetricValueList> entry:context.entrySet()) {

            // get the metrics from 1 & 7 days ago
            MetricValueList oneDayValues = reader.readMetrics(entry.getKey(), start + ONE_DAY_OFFSET  + (WINDOW_SIZE/-2), end + ONE_DAY_OFFSET);
            MetricValueList sevenDayValues = reader.readMetrics(entry.getKey(), start + SEVEN_DAYS_OFFSET  + (WINDOW_SIZE/-2), end + SEVEN_DAYS_OFFSET);

            if(oneDayValues.size() != sevenDayValues.size()) {
                LOG.warn("One and seven day lengths are not equal: {} != {}", oneDayValues.size(), sevenDayValues.size());
            }

            // shift the timestamps up
            oneDayValues.shiftTimestamp(ONE_DAY_OFFSET * -1);
            sevenDayValues.shiftTimestamp(SEVEN_DAYS_OFFSET * -1);

            final long newStart = start + MilliSecondUtils.minutesToMs(WINDOW_SIZE) * -1;

            // go back WINDOW_SIZE for the current values
            MetricValueList currentValues = new MetricValueList(newStart, entry.getValue().getEndTime());

            currentValues.addMetricValues(reader.readMetrics(entry.getKey(), newStart, start));
            currentValues.addMetricValues(entry.getValue());

            LOG.debug("CURRENT: {} to {}", MilliSecondUtils.msToString(currentValues.getStartTime()), MilliSecondUtils.msToString(currentValues.getEndTime()));
            LOG.debug("ONE    : {} to {}", MilliSecondUtils.msToString(oneDayValues.getStartTime()), MilliSecondUtils.msToString(oneDayValues.getEndTime()));
            LOG.debug("SEVEN  : {} to {}", MilliSecondUtils.msToString(sevenDayValues.getStartTime()), MilliSecondUtils.msToString(sevenDayValues.getEndTime()));

            SortedSet<Long> times = new TreeSet<Long>();

            // put all the times in the set so everything is normalized
            times.addAll(Collections2.transform(currentValues.getValues(), new GetTimestampFunction()));
            times.addAll(Collections2.transform(oneDayValues.getValues(), new GetTimestampFunction()));
            times.addAll(Collections2.transform(sevenDayValues.getValues(), new GetTimestampFunction()));

            // fill in any missing values
            currentValues = MetricValueList.fillInMissingValues(currentValues, times, true);
            oneDayValues = MetricValueList.fillInMissingValues(oneDayValues, times, true);
            sevenDayValues = MetricValueList.fillInMissingValues(sevenDayValues, times, true);

            // setup the stats
            final DescriptiveStatistics currentStats = new DescriptiveStatistics(Arrays.copyOf(currentValues.getDoubleValues(), WINDOW_SIZE));
            final DescriptiveStatistics oneDayStats = new DescriptiveStatistics(Arrays.copyOfRange(oneDayValues.getDoubleValues(), WINDOW_SIZE/2,  WINDOW_SIZE + 1));
            final DescriptiveStatistics sevenDayStats = new DescriptiveStatistics(Arrays.copyOfRange(sevenDayValues.getDoubleValues(), WINDOW_SIZE/2, WINDOW_SIZE + 1));

            currentStats.setWindowSize(WINDOW_SIZE);
            oneDayStats.setWindowSize(WINDOW_SIZE);
            sevenDayStats.setWindowSize(WINDOW_SIZE);

            final List<MetricValue> origValueList = currentValues.getValues();
            final List<MetricValue> oneDayValueList = oneDayValues.getValues();
            final List<MetricValue> sevenDayValueList = sevenDayValues.getValues();
            final List<MetricValue> highValues = new ArrayList<>();
            final List<MetricValue> lowValues = new ArrayList<>();

            // compute the high & low values
            for(int i=WINDOW_SIZE-2; i < oneDayValueList.size()-(WINDOW_SIZE/2); ++i) {
                final double prevValue = origValueList.get(i-1).getValue();

                // compute the prediction
                //double prediction = (currentStats.getVariance() + oneDayStats.getVariance() + sevenDayStats.getVariance()) / 3.0;
                double prediction = (3 * currentStats.getVariance() + 2 * sevenDayStats.getVariance() + oneDayStats.getVariance()) / 6.0;

                // set the high & low values
                highValues.add(new MetricValue(origValueList.get(i)).setValue(prevValue + (prediction/2)));
                lowValues.add(new MetricValue(origValueList.get(i)).setValue(prevValue - (prediction/2)));

                // update the stats
                currentStats.addValue(origValueList.get(i).getValue());
                oneDayStats.addValue(oneDayValueList.get(i+(WINDOW_SIZE/2)).getValue());
                sevenDayStats.addValue(sevenDayValueList.get(i+(WINDOW_SIZE/2)).getValue());
            }

            // add to ret
            ret.put(new DisplayMetricId(entry.getKey()).appendDisplayName(" PREDICTED HIGH"), new MetricValueList(highValues));
            ret.put(new DisplayMetricId(entry.getKey()).appendDisplayName(" PREDICTED LOW"), new MetricValueList(lowValues));
        }

        return ret;
    }

}
