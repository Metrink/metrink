package com.metrink.metric;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.metrink.utils.MilliSecondUtils.msToSeconds;
import static com.metrink.utils.MilliSecondUtils.msToString;
import static com.metrink.utils.MilliSecondUtils.roundDownToNSeconds;
import static com.metrink.utils.MilliSecondUtils.roundDownToSeconds;
import static com.metrink.utils.MilliSecondUtils.secondsToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metrink.metric.MetricValue.GetValueFunction;
import com.metrink.utils.AggregateFunction;
import com.metrink.utils.MilliSecondUtils;

/**
 * This class constructs a list of {@code MetricValue}s keeping track of unassigned times.
 *
 * Under the covers the class uses an array to attempt to be as lightweight as possible.
 *
 * This class is <b>NOT</b> thread safe.
 */
public class MetricValueList implements Iterable<MetricValue>, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MetricValueList.class);
    private static final long serialVersionUID = 1L;

    private final long startTimeInSeconds, endTimeInSeconds;
    private final long incInSeconds;
    private final MetricValue[] values;
    private List<MetricValue> nonNullValues; // this is a "cache" of the values that are non-null; set to null when dirty
    private int size = 0;

    /**
     * Constructs a MetricValueList given the starting and ending time in ms.
     * @param startTime the start time in ms.
     * @param endTime the end time in ms.
     */
    public MetricValueList(long startTime, long endTime) {
        // default is 1-minute buckets
        this(startTime, endTime, TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Constructs a MetricValueList given the starting and ending time in ms.
     * @param startTime the start time in ms.
     * @param endTime the end time in ms.
     * @param incInSeconds the increment between values in <b>SECONDS</b>.
     */
    public MetricValueList(long startTime, long endTime, long incInSeconds) {
        checkArgument(endTime >= startTime, "End time must be greater than or equal to startTime: %s : %s", startTime, endTime);
        checkArgument(incInSeconds > 0, "Increment in seconds must be greater than 1: %s", incInSeconds);

        this.startTimeInSeconds = msToSeconds(roundDownToNSeconds(startTime, incInSeconds));
        this.endTimeInSeconds = msToSeconds(roundDownToNSeconds(endTime, incInSeconds)); // this is OK because we add 1 later
        this.incInSeconds = incInSeconds;

        checkArgument(endTimeInSeconds >= startTimeInSeconds, "End time in seconds (%s) must be >= to start time in seconds (%s)", endTimeInSeconds, startTimeInSeconds);
        checkArgument(endTimeInSeconds - startTimeInSeconds + 1 < Integer.MAX_VALUE, "Range too long");

        values = new MetricValue[(int) ((endTimeInSeconds - startTimeInSeconds) / incInSeconds) + 1];

        //LOG.debug("Created metric value list: {} -> {} by {} values size: {}", startTimeInSeconds, endTimeInSeconds, incInSeconds, values.length);
    }

    /**
     * Given a list of {@link MetricValue}s creates a MetricValueList.
     *
     * This is basically equivalent to the other constructor and
     * a call to {@link #addMetricValues(Collection<MetricValue>)}.
     *
     * @param valueList the values to create the list with.
     */
    public MetricValueList(final List<MetricValue> valueList) {
        checkNotNull(valueList, "The value list must not be null");
        checkArgument(valueList.size() > 1, "The list must have at least 2 values");

        // make sure everything is sorted
        final List<MetricValue> sortedValueList = sortList(valueList);

        long tmpIncInSeconds = msToSeconds(sortedValueList.get(1).getTimestamp() - sortedValueList.get(0).getTimestamp());

        // attempt to find the min inc amount
        long prevTimestamp = sortedValueList.get(1).getTimestamp();
        for(int i=2; i < sortedValueList.size(); ++i) {
            final long curTimestamp = sortedValueList.get(i).getTimestamp();

            if(curTimestamp != prevTimestamp) {
                tmpIncInSeconds = FastMath.min(tmpIncInSeconds, roundDownToSeconds(curTimestamp - prevTimestamp));
            } else {
                LOG.warn("Found two values at the same timestamp: {}", curTimestamp);
            }

            // update prev timestamp
            prevTimestamp = sortedValueList.get(i).getTimestamp();
        }

        LOG.info("Determined increment size in seconds to be {}", tmpIncInSeconds);
        this.incInSeconds = tmpIncInSeconds;

        this.startTimeInSeconds = msToSeconds(sortedValueList.get(0).getTimestamp());
        this.endTimeInSeconds = msToSeconds(sortedValueList.get(sortedValueList.size()-1).getTimestamp());

        checkArgument(endTimeInSeconds >= startTimeInSeconds, "End time in seconds (%s) must be >= to start time in seconds (%s)", endTimeInSeconds, startTimeInSeconds);

        this.values = new MetricValue[(int) ((endTimeInSeconds - startTimeInSeconds) / incInSeconds) + 1];

        // now just add all the values
        this.addMetricValues(sortedValueList);
    }

    private int timeToIndex(long time) {
        final long timeInSeconds = msToSeconds(time);

        checkArgument(timeInSeconds >= startTimeInSeconds, "Time %s must be >= start time %s", secondsToString(timeInSeconds), secondsToString(startTimeInSeconds));
        checkArgument(timeInSeconds <= endTimeInSeconds, "Time %s must be <= to end time %s", secondsToString(timeInSeconds), secondsToString(endTimeInSeconds));

        if((timeInSeconds - startTimeInSeconds) % incInSeconds != 0) {
            LOG.warn("Timestamp does not fit perfectly in a bucket: {}", timeInSeconds);
        }

        return (int) ((timeInSeconds - startTimeInSeconds) / incInSeconds);
    }

    private long indexToTime(int index) {
        checkArgument(index >= 0, "Index (%s) must be greater than or equal to zero", index);
        checkArgument(index < values.length, "Index (%s) must be less than %s", index, values.length);

        return MilliSecondUtils.secondsToMs(startTimeInSeconds + index * incInSeconds);
    }

    public long getStartTime() {
        return MilliSecondUtils.secondsToMs(startTimeInSeconds);
    }

    public long getEndTime() {
        return MilliSecondUtils.secondsToMs(endTimeInSeconds);
    }

    /**
     * Returns the number of values in the list.
     * @return the number of values in the list.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the list is empty.
     * @return true if the list is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Recomputes the cached list ONLY if needed... always call!
     */
    private void recomputeCache() {
        if(nonNullValues == null) {
            // create a new list given the size
            final List<MetricValue> ret = new ArrayList<MetricValue>(size);

            for(MetricValue v:values) {
                if(v != null)
                    ret.add(v);
            }

            if(size != ret.size()) {
                LOG.error("The computed size and actual size do not match");
                size = ret.size();
            }

            nonNullValues = Collections.unmodifiableList(ret);
        }
    }

    /**
     * Adds a single Metric to the list.
     * @param metric the metric to add.
     * @return this
     */
    public MetricValueList addMetric(final Metric metric) {
        return addMetrics(Arrays.asList(metric));
    }

    /**
     * Adds a collection of metrics to a given list.
     * @param metrics the metrics to add to the list.
     * @return this
     */
    public MetricValueList addMetrics(final Collection<Metric> metrics) {
        checkNotNull(metrics, "Metrics must not be null");

        nonNullValues = null;

        // convert the iterator
        final Iterator<MetricValue> it =
                Iterators.transform(metrics.iterator(), new Function<Metric, MetricValue>() {
                    @Override
                    public MetricValue apply(Metric metric) {
                        return metric.getMetricValue();
                    }
                });

        // transform the values, and call addMetricValues
        return addMetricValues(it);
    }

    /**
     * Adds a single {@link MetricValue} to the list.
     * @param metricValue the metric value to add.
     * @return this
     */
    public MetricValueList addMetricValue(final MetricValue metricValue) {
        return addMetricValues(Arrays.asList(metricValue));
    }

    /**
     * Adds a {@link MetricValueList} to this {@link MetricValueList}.
     * @param metricValues the metrics to add.
     * @return this
     */
    public MetricValueList addMetricValues(final MetricValueList metricValues) {
        return addMetricValues(metricValues.getValues());
    }

    /**
     * Adds a collection of metric values to a given list.
     * @param metrics the metric values to add to the list.
     * @return this
     */
    public MetricValueList addMetricValues(final Collection<MetricValue> metricValues) {
        checkNotNull(metricValues, "Metric values must not be null");

        if(metricValues.isEmpty()) {
            return this;
        }

        return addMetricValues(metricValues.iterator());
    }

    /**
     * Adds a collection of metric values to the list.
     * @param it the iterator for the {@link MetricValue}s.
     * @return this
     */
    public MetricValueList addMetricValues(final Iterator<MetricValue> it) {
        checkNotNull(it, "Iterator must not be null");

        nonNullValues = null; // set our cache to null

        // go through the metrics adding the values
        while(it.hasNext()) {
            final MetricValue metric = it.next();
            final long timestamp = metric.getTimestamp();

            // get the position in the array
            final int pos = timeToIndex(timestamp);

            // check the range
            if(pos < 0 || pos >= values.length) {
                LOG.warn("Position outside of range, discarding value: {}", metric);
                continue;
            }

            // ensure it's blank
            checkArgument(values[pos] == null, "Attempting to overwrite a value at %s", pos);

            // increase our size, only if we're not overwriting
            // should really turn-on the above check
            if(values[pos] == null) {
                size++;
            }

            // set the MetricValue in the array ensuring the timestamp is normalized to seconds
            values[pos] = metric.setTimestamp(roundDownToSeconds(timestamp));
        }

        return this;
    }

    private List<MetricValue> sortList(final List<MetricValue> list) {
        List<MetricValue> ret = list;

        // if we cannot modify, then make a copy
        // otherwise just operate on the list
        if(list instanceof ImmutableList ||
           list instanceof UnmodifiableList) {
            ret = new ArrayList<MetricValue>(list);
        }

        // sort it
        Collections.sort(ret);

        return ret;
    }

    /**
     * Shifts all of the timestamps up by the offset.
     * @param offset the offset (which could be negative) to shift all the timestamps by.
     */
    public MetricValueList shiftTimestamp(final long offset) {
        nonNullValues = null; // set the cache to dirty

        for(MetricValue value:values) {
            if(value == null) {
                continue;
            }

            value.setTimestamp(value.getTimestamp() + offset);
        }

        return this;
    }

    /**
     * Gets a collection of {@link MetricValue}s that are in this list.
     * @return {@link MetricValue}s that are in the list.
     */
    public List<MetricValue> getValues() {
        recomputeCache();
        return nonNullValues;
    }

    /**
     * Constructs a copy of the {@link MetricValue}s that are in this list.
     * @return a copy of the {@link MetricValue}s that are in the list.
     */
    public List<MetricValue> getCopyOfValues() {
        recomputeCache();

        return new ArrayList<MetricValue>(Collections2.transform(nonNullValues, new Function<MetricValue, MetricValue>() {
            @Override
            public MetricValue apply(MetricValue arg) {
                return new MetricValue(arg);
            }
        }));
    }

    /**
     * Returns all of the non-null values in the list as a double array.
     *
     * <b>Note you loose all connection to timestamp, so care must be taken calling this function.</b>
     * @return all non-null values as a double array.
     */
    public double[] getDoubleValues() {
        return ArrayUtils.toPrimitive(Collections2.transform(getValues(), new GetValueFunction()).toArray(new Double[0]));
    }

    /**
     * Returns a Double Iterator over all of the non-null values in the list.
     *
     * <b>Note you loose all connection to timestamp, so care must be taken calling this function.</b>
     * @return an Iterator<Double> for the values in the list.
     */
    public Iterator<Double> getDoubleValuesIterator() {
        return new AbstractIterator<Double>() {
            private int i = -1;

            @Override
            protected Double computeNext() {
                do {
                    ++i;
                } while(i < values.length && values[i] == null);

                if(i < values.length) {
                    return values[i].getValue();
                } else {
                    return endOfData();
                }
            }
        };
    }

    /**
     * Gets the first {@link MetricValue} in the list.
     * @return the first {@link MetricValue} in the list.
     */
    public MetricValue getFirst() {
        return getValues().get(0);
    }

    /**
     * Gets the last {@link MetricValue} in the list.
     * @return the last {@link MetricValue} in the list.
     */
    public MetricValue getLast() {
        return getValues().get(size()-1);
    }

    /**
     * Aggregates the values over a duration of time using the provided function.
     *
     * For example if the list contains values for 30 days and the duration is 1 day,
     * then at most 30 values will be returned, and the function will be called at most 30 times.
     * There will be fewer than 30 values if a day is not represented in the list. The function will
     * always be called once per value in the returned list.
     *
     * @param duration the duration of time for a call to the function.
     * @param timeUnit the time unit of the duration.
     * @param function the aggregation function to use.
     *
     * @return a new list of {@link MetricValue}s that contains one value for each call to the function.
     */
    public List<MetricValue> aggregateByTime(final long duration, final TimeUnit timeUnit, final AggregateFunction<MetricValue> function) {
        final List<MetricValue> values = getValues();

        if(values.isEmpty()) {
            return Lists.newArrayList();
        } else if(values.size() == 1) {
            return Arrays.asList(function.aggregate(values)); // follow the contract, call the function once
        }

        final List<MetricValue> ret = new ArrayList<MetricValue>(values.size()/2); // we'll probably eliminate 1/2 of the items
        long durationInMs = timeUnit.toMillis(duration);

        int startIndex = 0;
        int curIndex = 1;

        while(curIndex < values.size()) {
            // if we've gone past a gap, then run the aggregation function
            if(values.get(curIndex).getTimestamp() - values.get(startIndex).getTimestamp() >= durationInMs) {
                ret.add(function.aggregate(values.subList(startIndex, curIndex)));
                startIndex = curIndex;
            }

            curIndex++;
        }

        // add the last one to the list
        ret.add(function.aggregate(values.subList(startIndex, curIndex)));

        return ret;
    }

    /**
     * Aggregates the values given the number of values per aggregation call.
     *
     * For example if the list contains 20 values and {@code valuesPerAggregation} is 5,
     * then the function will be called 4 times and the returned list will contain 4 values.
     *
     * @param valuesPerAggregation the maximum number of values to include in each aggregation call.
     * @param function the aggregation function to use.
     *
     * @return a new list of {@link MetricValue}s that contains one value for each call to the function.
     */
    public List<MetricValue> aggregateByValuesInAggregation(int valuesPerAggregation, final AggregateFunction<MetricValue> function) {
        final List<MetricValue> values = getValues();

        if(values.isEmpty()) {
            return Lists.newArrayList();
        }

        final List<List<MetricValue>> partitions = Lists.partition(values, valuesPerAggregation);
        final List<MetricValue> ret = new ArrayList<MetricValue>(partitions.size());

        for(List<MetricValue> partition:partitions) {
            ret.add(function.aggregate(partition));
        }

        return ret;
    }

    /**
     * Aggregates the values given the number of aggregation calls.
     *
     * For example if the list contains 20 values and {@code valuesInResult} is 5,
     * then the function will be called 5 times and the returned list will contain 5 values.
     *
     * @param valuesPerAggregation the maximum number of values to include in each aggregation call.
     * @param function the aggregation function to use.
     *
     * @return a new list of {@link MetricValue}s that contains one value for each call to the function.
     */
    public List<MetricValue> aggregateByValuesInResult(int valuesInResult, final AggregateFunction<MetricValue> function) {
        final int valuesPerAggregation = (int) Math.ceil(size()/(double)valuesInResult);

        if(isEmpty()) {
            return Lists.newArrayList();
        } if(valuesPerAggregation == 0) {
            return Arrays.asList(function.aggregate(getValues())); // follow the contract, call the function once
        }

        return aggregateByValuesInAggregation(valuesPerAggregation, function);
    }

    @Override
    public ListIterator<MetricValue> iterator() {
        return this.getValues().listIterator();
    }

    @Override
    public String toString() {
        return this.getValues().toString();
    }

    /**
     * Given a {@link MetricValueList} go through and try to fill in all the missing values.
     *
     * This function will pad the front and end with copies of the first non-null value.
     * For any value in the middle, it will use the average when useAverage is true.
     * The {@link MetricValueList} that is passed in is NOT modified in anyway.
     *
     * @param metricValueList the {@link MetricValueList} to convert.
     * @param requiredTimes the times required to be in this list in ms rounded to the nearest second.
     * @param useAverage true if the average value should be used for gaps in the middle, otherwise zero is used.
     * @return a list of {@link MetricValue}s where all values are non-null and all times (in seconds) exist.
     */
    public static MetricValueList fillInMissingValues(final MetricValueList metricValueList, final SortedSet<Long> requiredTimes, final boolean useAverage) {
        // sanity checks
        checkNotNull(metricValueList, "MetricValueList is null");
        checkNotNull(requiredTimes, "Required times is null");
        checkArgument(metricValueList.size() > 0, "MetricValueList is empty");
        checkArgument(! (metricValueList.size() == 1 && metricValueList.values[0] == null), "MetricValueList is all nulls");

        // create the new metric value list possibly re-sizing to the correct range
        final MetricValueList ret = new MetricValueList(requiredTimes.first(), requiredTimes.last());

        // add in all the existing metric values
        ret.addMetricValues(metricValueList);

        MetricValue[] values = ret.values;

        MetricValue lastNonNull = null;

        // go through all the values
        for(int i=0; i < values.length; ++i) {
            if(values[i] != null) {
                lastNonNull = values[i];
                continue;
            }

            // find the first non-null value going forward
            int j=i;
            for(; j < values.length && values[j] == null; ++j);

            // make sure we don't have all null values
            if(i == 0 && j == values.length) {
                throw new IllegalArgumentException("MetricValueList is all null");
            }

            // go through the values in the gap
            for(int k=i; k < j; ++k) {
                final long ts = ret.indexToTime(k);

                // if it is not a required time, then move along
                if(!requiredTimes.contains(ts)) {
                    continue;
                }

                // check to see if we need to use the average
                if(useAverage) {
                    if(i == 0) { // if it's the first one, we cannot average
                        values[k] = new MetricValue(ts, values[j].getValue(), values[j].getUnits());
                    } else if(j == values.length) { // if it's the last, we use the last known good value
                        values[k] = new MetricValue(ts, lastNonNull.getValue(), lastNonNull.getUnits());
                    } else { // somewhere in the middle
                        final double value = (lastNonNull.getValue() + values[j].getValue()) / 2;
                        values[k] = new MetricValue(ts, value, values[j].getUnits());
                    }
                } else { // we should just use zero
                    if(j == values.length) {
                        values[k] = new MetricValue(ts, 0.0, lastNonNull.getUnits());
                    } else {
                        values[k] = new MetricValue(ts, 0.0, values[j].getUnits());
                    }
                }

                ret.size++; // we "added" a value here, so we need to increment our size

                // sanity check
                if(ret.indexToTime(k) != values[k].getTimestamp()) {
                    LOG.error("Error set wrong timestamp {} at index {}", values[k].getTimestamp(), k);
                }

                LOG.debug("Added missing value {} at {}", values[k].getValue(), msToString(values[k].getTimestamp()));
            }

            i = j; // update our value of i

            if(j < values.length) {
                lastNonNull = values[j]; // update our last non-null
            }
        }

        ret.nonNullValues = null; // just to be safe

        return ret;
    }
}
