package com.metrink.stats;

import java.util.Arrays;
import java.util.SortedSet;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.metrink.metric.MetricValueList;
import com.metrink.metric.MetricValue.GetTimestampFunction;

/**
 * Correlation class used to determine the correlation between two data sets.
 */
public class Correlation {
    public static final Logger LOG = LoggerFactory.getLogger(Correlation.class);

    final static PearsonsCorrelation pearsons = new PearsonsCorrelation();


    /**
     * Computes correlation of two {@link MetricValueList}.
     * @param lhs the metric list
     * @param rhs the other metric list
     * @return value from -1 to 1: 1 is a perfect correlation, 0 is no correlation, and -1 is an inverse correlation
     */
    public double correlation(final MetricValueList lhs, final MetricValueList rhs) {

        // Skip the correlation if either list is empty. Pearsons requires at least one point.
        if (lhs.size() < 2 || rhs.size() < 2) {
            return 0;
        }
        // NOTE: This implementation gobbles memory and is over all not efficient. The requiredTimes logic should
        // probably be moved to the callee or perhaps added to MetricValueList.

        final SortedSet<Long> requiredTimes = Sets.newTreeSet();

        requiredTimes.addAll(Collections2.transform(lhs.getValues(), new GetTimestampFunction()));
        requiredTimes.addAll(Collections2.transform(rhs.getValues(), new GetTimestampFunction()));

        final double[] rawLhs = MetricValueList.fillInMissingValues(lhs, requiredTimes, true).getDoubleValues();
        final double[] rawRhs = MetricValueList.fillInMissingValues(rhs, requiredTimes, true).getDoubleValues();

        final double correlation = pearsons.correlation(rawLhs, rawRhs);

        LOG.debug("Correlation on lists of {} and {} is {}", rawLhs.length, rawRhs.length, correlation);

        // Correlation can be set to NaN if one of the lines is flat
        return Double.isNaN(correlation) ? 0 : correlation;
    }

    /**
     * Computes numerous correlations using a sliding time window.
     * @param lhs one set of metrics.
     * @param rhs the other set of metrics.
     * @return the best correlation found.
     */
    public double correlationWithTimeShift(final MetricValueList lhs,
                                           final MetricValueList rhs) {

        // Skip the correlation if either list is empty. Pearsons requires at least one point.
        if (lhs.size() < 2 || rhs.size() < 2) {
            return 0;
        }

        final SortedSet<Long> requiredTimes = Sets.newTreeSet();

        requiredTimes.addAll(Collections2.transform(lhs.getValues(), new GetTimestampFunction()));
        requiredTimes.addAll(Collections2.transform(rhs.getValues(), new GetTimestampFunction()));

        final double[] lhsValues = MetricValueList.fillInMissingValues(lhs, requiredTimes, true).getDoubleValues();
        final double[] rhsValues = MetricValueList.fillInMissingValues(rhs, requiredTimes, true).getDoubleValues();

        /*
         * Compute last half and first half:
         * RHS:     |***-***|
         * LHS: |***-***|
         */
        double corr1 =
            pearsons.correlation(Arrays.copyOfRange(lhsValues, (int)FastMath.floor(lhsValues.length/2.0), lhsValues.length),
                                 Arrays.copyOfRange(rhsValues, 0, (int)FastMath.ceil(rhsValues.length/2.0)));

        /*
         * Compute 2/3rds:
         * RHS:    |**-**-**|
         * LHS: |**-**-**|
         */
        double corr2 =
            pearsons.correlation(Arrays.copyOfRange(lhsValues, lhsValues.length/3, lhsValues.length),
                                 Arrays.copyOfRange(rhsValues, 0, (int)FastMath.ceil((2*rhsValues.length)/3.0)));

        /*
         * Compute whole thing
         * RHS: |****|
         * LHS: |****|
         */
        double corr3 = pearsons.correlation(lhsValues, rhsValues);

        return returnMaxMagnitude(corr1, corr2, corr3);
    }

    /**
     * Given any number of doubles, return the one that has the largest magnitude.
     * @param args the values to compare.
     * @return the argument with the largest magnitude.
     */
    public static double returnMaxMagnitude(double ... args) {
        if(args.length == 0) {
            throw new IllegalArgumentException("Must pass some arguments");
        } else if(args.length == 1) {
            return args[0];
        }

        double maxMag = FastMath.abs(args[0]);
        double ret = args[0];

        for(int i=1; i < args.length; ++i) {
            if(FastMath.abs(args[i]) > maxMag) {
                maxMag = FastMath.abs(args[i]);
                ret = args[i];
            }
        }

        return ret;
    }


    /**
     * Computes an approximate Pearson correlation coefficients using a DFT.
     * @param array1 sample 1.
     * @param array2 sample 2.
     * @return An approximate Pearson correlation coefficient that is always > than the actual.
     * @deprecated Not actually deprecated, but I'm flagging it as such because negative correlated tests are failing
     */
    @Deprecated
    public double approximateCorrelation(final double[] array1, final double[] array2) {
        final double[] fftArray1 = DiscreteFourierTransform.forward(normalize(array1));
        final double[] fftArray2 = DiscreteFourierTransform.forward(normalize(array2));

        return MathArrays.distance(fftArray1, fftArray2);
    }

    /**
     * Computes a Pearsons Correlation of two samples.
     * @param array1 sample 1
     * @param array2 sample 2
     * @return The Pearson Correlation Coefficient
     */
    public double correlation(final double[] array1, final double[] array2) {
        return 1 - 0.5 * FastMath.pow(distance(normalize(array1), normalize(array2)), 2.0);
    }

    public double correlation(final double[] array1, final int start1, final double[] array2, final int start2, final int length) {
        return 1 - 0.5 * FastMath.pow(distance(normalize(array1, start1, start1 + length), normalize(array2, start2, start2 + length)), 2.0);
    }


    public double distance(final double[] array1, final double[] array2) {
        if(array1.length != array2.length) {
            throw new IllegalArgumentException("Arrays must be the same length");
        }

        return distance(array1, 0, array2, 0, array2.length);
    }

    public double distance(final double[] array1, final int start1, final double[] array2, final int start2, final int length) {
        if(start1 < 0 || start1 > array1.length || length > array1.length) {
            throw new IndexOutOfBoundsException("Start1 and/or end1 is invalid");
        }

        if(start2 < 0 || start2 > array2.length || length > array2.length) {
            throw new IndexOutOfBoundsException("Start2 and/or end2 is invalid");
        }

        double ret = 0.0;

        for(int i=start1, j=start2; i < length; ++i, ++j) {
            ret += FastMath.pow(array1[i] - array2[j], 2);
        }

        return FastMath.sqrt(ret);
    }

    public double[] normalize(final double[] arg) {
        return normalize(arg, 0, arg.length);
    }

    public double[] normalize(final double[] arg, final int start, final int end) {
        if(start < 0 || start > end || end < start || end > arg.length) {
            throw new IndexOutOfBoundsException("Start and/or end is invalid");
        }

        final SummaryStatistics stats = new SummaryStatistics();
        final double[] ret = new double[end - start];

        for(int i=start; i < end; ++i) {
            stats.addValue(arg[i]);
        }

        final double mean = stats.getMean();
        final double sigma = sigma(arg, mean);

        for(int i=start; i < end; ++i) {
            ret[i-start] = (arg[i] - mean) / sigma;
        }

        return ret;
    }

    private double sigma(final double[] arg, final double mean) {
        return sigma(arg, 0, arg.length, mean);
    }

    private double sigma(final double[] arg, final int start, final int end, final double mean) {
        if(start < 0 || start > end || end < start || end > arg.length) {
            throw new IndexOutOfBoundsException("Start and/or end is invalid");
        }

        double ret = 0.0;

        for(int i=start; i < end; ++i) {
            ret += FastMath.pow(arg[i] - mean, 2);
        }

        return FastMath.sqrt(ret);
    }
}
