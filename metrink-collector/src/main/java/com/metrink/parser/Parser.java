package com.metrink.parser;

import java.util.List;

import com.metrink.metric.Metric;


public interface Parser {

    /**
     * Given an array of bytes, return a Metric.
     *
     * This method MUST be thread safe!
     * @param data the data to convert to a metric.
     */
    public List<Metric> parse(byte[] data) throws ParserException;
}
