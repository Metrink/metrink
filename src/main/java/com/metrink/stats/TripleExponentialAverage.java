package com.metrink.stats;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.metrink.utils.DoubleRingBuffer;

public class TripleExponentialAverage {
    public static final Logger LOG = LoggerFactory.getLogger(TripleExponentialAverage.class);

    public static final int DEFAULT_PERIOD = (int) TimeUnit.DAYS.toMinutes(7);

    private static final double alpha = 0.5;
    private static final double beta = 0.6; // beta & gamma follow Wikipedia, not NIST
    private static final double gamma = 0.4;

    private int period;
    private double forecast = 0.0; // this is the value we predict will come next
    private double trending = 0; // we punt on this and say it's not rising or falling currently
    private double smoothing = 0;
    private final DoubleRingBuffer seasonal;
    private double[] historicValues;
    private double currentForecast;

    /**
     * Constructs a TripleExponentialAverage given historic values using a period of 7 days in minutes.
     * @param historicValues the historic values to use.
     */
    public TripleExponentialAverage(double[] historicValues) {
        this(historicValues, DEFAULT_PERIOD);
    }

    /**
     * Constructs a TripleExponentialAverage given historic values and a period.
     * @param historicValues the historic values to use.
     * @param period the period of the values.
     */
    public TripleExponentialAverage(double[] historicValues, int period) {
        this.period = period;
        checkArgument(historicValues.length >= period * 2, "The length of historic values (%s) must be greater than twice the period (%s)", historicValues.length, period*2);

        this.seasonal = new DoubleRingBuffer(Arrays.copyOf(historicValues, period));
        this.historicValues = Arrays.copyOf(historicValues, historicValues.length);

        smoothing = historicValues[0];
        trending = computeInitialTrending();
        computeInitialSeasonal();

        LOG.debug("INIT SMOOTH: {} TREND: {} SEASONAL: {}", new Object[] { smoothing, trending, Doubles.join(",", seasonal.getArray()) });

        // now go through the historic values computing forecasts
        for(int i=2; i < historicValues.length; ++i) {
            currentForecast = computeForecast(historicValues[i], i - period < 0);
        }
    }

    /**
     * Given a value, computes the forecasted value updating the internal state.
     * @param value the current value.
     * @return the forecasted value.
     */
    public double computeForecast(double value) {
        final double ret = currentForecast;

        currentForecast = computeForecast(value, false);

        return ret;
    }

    /**
     * Given a value, computes the forecasted <b>next</b> value updating the internal state.
     * @param value the current value.
     * @param init if this is part of the historic initialization.
     * @return the forecasted next value.
     */
    private double computeForecast(double value, boolean init) {
        final double oldSmoothing = smoothing;
        final double oldTrending = trending;
        final double oldSeasonal = init ? seasonal.get(period-1) : seasonal.get();

        //
        // Will want to check that oldSeasonal & smoothing aren't zero
        // and if so, switch to additive method?
        //

        if(init) {
            smoothing = alpha * value + (1.0 - alpha) * (oldSmoothing + oldTrending);
        } else {
            smoothing = alpha * (value / oldSeasonal) + (1.0 - alpha) * (oldSmoothing + oldTrending);
        }

        trending = beta * (smoothing - oldSmoothing) + (1.0 - beta) * oldTrending;


        if(!init) {
            double newSeasonal = gamma * (value / smoothing) + (1.0 - gamma) * oldSeasonal;
            seasonal.add(newSeasonal);
        }

        forecast = init ? 0.0 : (smoothing + trending) * seasonal.get();

        LOG.trace("SEASON: {} {}", Doubles.join(",", seasonal.getArray()), seasonal.get());
        LOG.trace("V: {} S: {} T: {} S: {} F: {}",
                new Object[] { value, smoothing, trending, seasonal.get(period-1), forecast });


        return forecast;
    }

    private double computeInitialTrending() {
        double ret = 0.0;

        for(int i=0; i < period; ++i) {
            ret += historicValues[period + i] - historicValues[i];
        }

        return ret / (period * period);
    }

    private void computeInitialSeasonal() {
        final int seasons = historicValues.length / period;

        final double[] seasonalAverage = new double[seasons];
        final double[] averagedObservations = new double[historicValues.length];

        for(int s=0; s < seasons; ++s) {
            for(int i=0; i < period; ++i) {
                seasonalAverage[s] += historicValues[(s * period) + i];
            }
            seasonalAverage[s] /= period;
        }

        for (int s = 0; s < seasons; s++) {
            for (int i = 0; i < period; i++) {
                averagedObservations[(s * period) + i] = historicValues[(s * period) + i] / seasonalAverage[s];
            }
        }


        for (int p = 0; p < period; p++) {
            double seasonalValue = 0.0;

            for (int s = 0; s < seasons; s++) {
                seasonalValue += averagedObservations[(s * period) + p];
            }

            seasonal.add(seasonalValue / seasons);
        }

    }

}
