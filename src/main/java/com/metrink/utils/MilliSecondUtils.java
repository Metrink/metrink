package com.metrink.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for dealing with longs that represent epoch.
 *
 * All inputs are Java epoch (ms since 1970...) and outputs are the same, unless otherwise noted.
 */
public class MilliSecondUtils {
    public static final Logger LOG = LoggerFactory.getLogger(MilliSecondUtils.class);

    public static final long SEC_IN_MS = TimeUnit.SECONDS.toMillis(1);
    public static final long MIN_IN_MS = TimeUnit.MINUTES.toMillis(1);
    public static final long DAY_IN_MS = TimeUnit.DAYS.toMillis(1);

    private static final DateTimeFormatter YYYYMM_FORMAT = DateTimeFormat.forPattern("YYYYMM").withZoneUTC();
    public static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();

    private static final DateTimeFormatter QUERY_DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");

    private MilliSecondUtils() { }

    /**
     * Rounds a MS epoch down to the nearest second.
     * @param ms the epoch.
     * @return the epoch rounded down to the second.
     */
    public static long roundDownToSeconds(final long ms) {
        return (ms / SEC_IN_MS) * SEC_IN_MS;
    }

    public static long roundDownToNSeconds(final long ms, final long n) {
        final long secsInMs = TimeUnit.SECONDS.toMillis(n);

        return (ms / secsInMs) * secsInMs;
    }

    /**
     * Rounds a MS epoch down to the minute.
     * @param ms the epoch.
     * @return the epoch rounded down to the minute.
     */
    public static long roundDown1Minute(final long ms) {
        return (ms / MIN_IN_MS) * MIN_IN_MS;
    }

    public static long roundDownNMinutes(final long ms, final int n) {
        final long minsInMs = TimeUnit.MINUTES.toMillis(n);

        return (ms / minsInMs) * minsInMs;
    }

    /**
     * Rounds a MS epoch down to the day.
     * @param ms the epoch.
     * @return the epoch rounded down to the day.
     */
    public static long roundDown1Day(final long ms) {
        return (ms / DAY_IN_MS) * DAY_IN_MS;
    }

    /**
     * Rounds a MS epoch up to the nearest second.
     * @param ms the epoch.
     * @return the epoch rounded down to the second.
     */
    public static long roundUp1Second(final long ms) {
        return (long) FastMath.ceil(ms / (double)SEC_IN_MS) * SEC_IN_MS;
    }

    /**
     * Rounds a MS epoch down to the minute.
     * @param ms the epoch.
     * @return the epoch rounded down to the minute.
     */
    public static long roundUp1Minute(final long ms) {
        return (long) FastMath.ceil(ms / (double)MIN_IN_MS) * MIN_IN_MS;
    }

    /**
     * Converts a MS epoch into number of minutes.
     * @param ms the epoch.
     * @return the number of minutes since 1970, <b>NOT</b> an epoch.
     */
    public static long msToMinutes(final long ms) {
        return ms/MIN_IN_MS;
    }

    /**
     * Converts the number of minutes since 1970 to an epoch.
     * @param minutes the minutes since 1970.
     * @return an epoch.
     */
    public static long minutesToMs(final long minutes) {
        return minutes * MIN_IN_MS;
    }

    /**
     * Converts a MS epoch into number of seconds.
     * @param ms the epoch.
     * @return the number of seconds since 1970, <b>NOT</b> an epoch.
     */
    public static long msToSeconds(final long ms) {
        return ms/SEC_IN_MS;
    }

    /**
     * Converts the number of seconds since 1970 to an epoch.
     * @param seconds the seconds since 1970.
     * @return an epoch.
     */
    public static long secondsToMs(final long seconds) {
        return seconds * SEC_IN_MS;
    }

    /**
     * Given milliseconds since epoch, get the year and month as a string: YYYYMM.
     * @param millis the millis since epoch
     * @return year as integer
     */
    public static String millisToYearMonth(final long millis) {
        return YYYYMM_FORMAT.print(millis);
    }

    /**
     * Generates a "range" of YYYYMM given a start and end.
     * @param start the first time in the "range".
     * @param end the last time in the "range".
     * @return a Set of strings that represent the range.
     */
    public static SortedSet<String> generateYearMonthSet(final long start, final long end) {
        checkArgument(start <= end, "Start %s must be less than or equal to end %s", start, end);

        final SortedSet<String> ret = new TreeSet<String>();
        DateTime cur = new DateTime(start, DateTimeZone.UTC);

        while(cur.isBefore(end)) {
            ret.add(millisToYearMonth(cur.getMillis()));
            cur = cur.plusMonths(1);
        }

        // make sure to get end in the set
        ret.add(millisToYearMonth(end));

        return ret;
    }

    /**
     * Given an epoch in MS, returns a string representing the date.
     * @param ms the epoch.
     * @return the string representing the epoch.
     */
    public static String msToString(final long ms) {
        return FULL_DATE_FORMAT.print(ms);
    }

    /**
     * Given an epoch in MS, returns a string representing the date for eastern time.
     * @param ms the epoch.
     * @return the string representing the epoch.
     */
    public static String msToQueryTime(final long ms, final DateTimeZone timeZone) {
        return QUERY_DATE_FORMAT.withZone(timeZone).print(roundDown1Minute(ms));
    }

    /**
     * Given an epoch in minutes, returns a string representing the date.
     * @param minutes the epoch.
     * @return the string representing the epoch.
     */
    @Deprecated
    public static String minutesToString(final long minutes) {
        return msToString(minutesToMs(minutes));
    }

    /**
     * Given an epoch in minutes, returns a string representing the date.
     * @param seconds the epoch.
     * @return the string representing the epoch.
     */
    public static String secondsToString(final long seconds) {
        return msToString(secondsToMs(seconds));
    }

}
