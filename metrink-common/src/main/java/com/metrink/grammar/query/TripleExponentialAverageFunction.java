package com.metrink.grammar.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.metric.DisplayMetricId;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricRequest;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricReader;
import com.metrink.stats.TripleExponentialAverage;

public class TripleExponentialAverageFunction extends QueryFunction {
    public static final Logger LOG = LoggerFactory.getLogger(TripleExponentialAverageFunction.class);

    private final MetricReader metricReader;

    @Inject
    public TripleExponentialAverageFunction(final MetricReader metricReader) {
        this.metricReader = metricReader;
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException{
        final Map<MetricId, MetricValueList> ret = new HashMap<>();

        for(Map.Entry<MetricId, MetricValueList> entry:context.entrySet()) {
            final int period = (int) TimeUnit.DAYS.toMinutes(1);

            final MetricValueList values = entry.getValue();
            final double[] historicValues = fetchHistoricValues(entry.getKey(), values.getStartTime(), period);
            final TripleExponentialAverage average = new TripleExponentialAverage(historicValues, period);

            // setup the values for the upper & lower graph lines
            final MetricValueList upperValues = new MetricValueList(values.getStartTime(), values.getEndTime());
            final MetricValueList lowerValues = new MetricValueList(values.getStartTime(), values.getEndTime());

            for(MetricValue value:entry.getValue()) {
                // make a copy of the value
                final MetricValue v = new MetricValue(value);

                // compute the forecast
                final double forecast = average.computeForecast(value.getValue());

                // add the upper value
                v.setValue(forecast * 1.10);
                upperValues.addMetricValue(v);

                // add the lower value
                v.setValue(forecast * 0.90);
                lowerValues.addMetricValue(v);
            }

            // add the lines to ret
            ret.put(new DisplayMetricId(entry.getKey()).appendDisplayName(" upper bound"), upperValues);
            ret.put(new DisplayMetricId(entry.getKey()).appendDisplayName(" lower bound"), lowerValues);
        }

        return ret;
    }

    /**
     * Fetches MINUTES_IN_SEASON values from now back in time MINUTES_IN_SEASON.
     * @return historic values.
     */
    private double[] fetchHistoricValues(MetricId metricId, long end, int period) {
        final long start = new DateTime(end).minusMinutes(period * 2).getMillis();
        final double[] historicValues = new double[period * 2];

        // construct a MetricRequest
        final MetricRequest request = new MetricRequest(metricId, start, end);

        // fetch the metrics
        final Map<MetricId, MetricValueList> res = metricReader.readMetrics(Arrays.asList(request));

        final List<MetricValue> metricValues = res.get(metricId).getValues();

        double curValue = 0.0;
        int pos = 0;
        final PeekingIterator<MetricValue> valueIt = Iterators.peekingIterator(metricValues.iterator());

        // go through all of the results adding in gaps if needed
        for(long curTime = start; curTime < end; curTime += TimeUnit.MINUTES.toMillis(1)) {

            // we update the current value if the times are the same
            if(valueIt.peek().getTimestamp() == curTime) {
                curValue = valueIt.next().getValue();
            }

            // set the value in the historicValues array
            historicValues[pos++] = curValue;
        }

        return historicValues;
    }

}
