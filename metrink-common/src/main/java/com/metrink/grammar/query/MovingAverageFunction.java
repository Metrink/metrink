package com.metrink.grammar.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metrink.grammar.Argument;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.NumberArgument;
import com.metrink.grammar.RelativeTimeArgument;
import com.metrink.metric.MetricId;
import com.metrink.metric.MetricValue;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.io.MetricReader;
import com.metrink.utils.MilliSecondUtils;


public class MovingAverageFunction extends QueryFunction {
    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageFunction.class);

    private final MetricReader reader;
    private int windowSize = 5; // default to 5 minutes?

    @Inject
    public MovingAverageFunction(MetricReader reader) {
        this.reader = reader;
    }

    @Override
    public Map<MetricId, MetricValueList> process(final long start, final long end, final ImmutableMap<MetricId, MetricValueList> context) throws MetrinkParseException {
        Map<MetricId, MetricValueList> ret = new HashMap<>();

        // setup our window size based upon the argument
        computeWindowSize();

        for(Map.Entry<MetricId, MetricValueList> entry:context.entrySet()) {
            if(entry.getValue().size() == 0) {
                ret.put(createNewId(entry.getKey()), entry.getValue());
                continue;
            }

            // get the old metrics for this ID
            final MetricValueList oldValues = reader.readMetrics(entry.getKey(),
                                                                 start - MilliSecondUtils.minutesToMs(windowSize),
                                                                 start);

            final DescriptiveStatistics stats = new DescriptiveStatistics(oldValues.getDoubleValues());
            stats.setWindowSize(windowSize); // set our window size

            final List<MetricValue> newValues = entry.getValue().getCopyOfValues();
            final double[] values = entry.getValue().getDoubleValues();

            // newValues.size() should equal values.length
            if(newValues.size() != values.length) {
                LOG.warn("Values as list and as array are NOT the same size: {} != {}", newValues.size(), values.length);
            }

            // we do a set before adding to our stats because we've already primed our stats with old values
            for(int i=0; i < FastMath.min(newValues.size(), values.length); ++i) {
                // set the value to the moving average
                newValues.get(i).setValue(stats.getMean());

                // add a new value to our stats
                stats.addValue(values[i]);
            }

            // set the value to the moving average
            //newValues.get(i).setValue(stats.getMean());

            ret.put(createNewId(entry.getKey()), new MetricValueList(newValues));
        }

        return ret;
    }

    // given the arguments, compute the window size... it'll always be positive
    private void computeWindowSize() throws MetrinkParseException {
        if(this.getArgs().isEmpty()) {
            return;
        }

        if(this.getArgs().size() != 1) {
            throw new MetrinkParseException("Moving average takes either one or zero arguments");
        }

        Argument arg = this.getArgs().get(0);

        if(arg instanceof NumberArgument) {
            this.windowSize = ((NumberArgument) arg).getInt();

            if(windowSize < 2) {
                throw new MetrinkParseException("Moving average window size is too small");
            }
        } else if(arg instanceof RelativeTimeArgument) {
            this.windowSize = (int) ((RelativeTimeArgument) arg).getTimeUnit().toMinutes(((RelativeTimeArgument) arg).getDuration());

            if(this.windowSize < 0) {
                this.windowSize *= -1; // we'll fix this for the user
            }

            if(this.windowSize < 2) {
                throw new MetrinkParseException("Moving average window size is too small");
            }
        } else {
            throw new MetrinkParseException("Moving average takes either a number or a relative time as an argument");
        }
    }

}
