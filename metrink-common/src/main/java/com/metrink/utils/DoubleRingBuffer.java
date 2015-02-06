package com.metrink.utils;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

/**
 * A ring buffer that can only hold doubles, and must be filled upon creation.
 */
public class DoubleRingBuffer {
    public static final Logger LOG = LoggerFactory.getLogger(DoubleRingBuffer.class);

    private double[] buffer;
    private int curPos;

    public DoubleRingBuffer(final double[] values) {
        buffer = new double[values.length];
        curPos = 0;

        // copy all the values in
        System.arraycopy(values, 0, buffer, 0, values.length);
    }

    /**
     * Adds a value to the buffer overwriting old values
     * @param val the value to add to the buffer.
     */
    public void add(double val) {
        buffer[curPos] = val;

        curPos = curPos + 1 >= buffer.length ? 0 : curPos + 1;
    }

    /**
     * Gets the "head" value from the buffer without incrementing the position.
     * @return the "head" value, or oldest value in the buffer.
     */
    public double get() {
        return buffer[curPos];
    }

    /**
     * Gets the value at a certain position.
     * @param pos the position in the array to get the value.
     * @return the value at that position.
     */
    public double get(int pos) {
        pos = curPos + pos;

        return buffer[pos >= buffer.length ? pos - buffer.length : pos];
    }

    /**
     * Gets the buffer as an array.
     * <b>This is an expensive call!</b>
     * @return the array of doubles for this buffer.
     */
    public double[] getArray() {
        return Doubles.concat(Arrays.copyOfRange(buffer, curPos, buffer.length), Arrays.copyOfRange(buffer, 0, curPos));
    }

}
