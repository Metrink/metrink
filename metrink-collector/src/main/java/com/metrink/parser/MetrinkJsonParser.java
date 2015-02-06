package com.metrink.parser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.inject.DateTimeProvider;
import com.metrink.metric.Metric;

/**
 * Parser for handling Metrink JSON input.
 */
public class MetrinkJsonParser implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(MetrinkJsonParser.class);
    private static final DateTimeFormatter ISO_FORMATTER = ISODateTimeFormat.dateTime();

    private final DateTimeProvider dateTimeProvider;

    @Inject
    public MetrinkJsonParser(final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public List<Metric> parse(final byte[] data) throws ParserException {
        JSONObject json = null;
        String jsonString = null;

        try {
            jsonString = new String(data, "UTF-8");
            json = new JSONObject(jsonString);
        } catch (final JSONException e) {
            LOG.error("Error parsing JSON: {}", e.getMessage());
            LOG.debug("JSON: {}", jsonString);
            throw new ParserException(e);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding found while parsing JSON: {}", e.getMessage());
            throw new ParserException(e.getMessage());
        }

        // construct the list we'll eventually return
        final List<Metric> ret = new ArrayList<Metric>();

        // get the device field
        final String device = getField(json, "d");

        // get the metrics array
        JSONArray metrics = null;

        try {
            metrics = json.getJSONArray("m");
        } catch (final JSONException e) {
            LOG.error("Error getting metrics array: {}", e.getMessage());
            throw new ParserException(e);
        }

        if(metrics == null || metrics.length() == 0) {
            LOG.error("Error metrics array null or size is zero");
            return ret;
        }

        for(int i=0; i < metrics.length(); ++i) {

            try {
                final JSONObject m = metrics.getJSONObject(i);

                final Metric metric = new Metric(device,
                                                 getField(json, "g", m),
                                                 getField(m, "n"),
                                                 m.has("t") ? m.getLong("t") : dateTimeProvider.get().getMillis(),
                                                 m.getDouble("v"),
                                                 // should really validate units as well
                                                 m.has("u") ? m.getString("u") : "");

                ret.add(metric);
            } catch (final JSONException e) {
                LOG.warn("Error getting JSON value: {}", e.getMessage());
                continue;
            }

        }

        return ret;
    }

    private String getField(final JSONObject json, final String field) throws ParserException {
        return getField(json, field, new JSONObject());
    }

    private String getField(final JSONObject json, final String field, final JSONObject defaultJson)  throws ParserException {
        String res = null;

        try {
            res = json.has(field) ? json.getString(field) : defaultJson.getString(field);
        } catch (final JSONException e) {
            LOG.error("Error getting {}: {}", field, e.getMessage());
            throw new ParserException(e, field);
        }

        //
        // Sanity check the value
        //
        if(StringUtils.isBlank(res)) {
            LOG.error("Error {} is blank", field);
            throw new ParserException(field + " is blank");
        }

        // check first that we have printable characters
        if(!StringUtils.isAsciiPrintable(res)) {
            LOG.error("Error {} contains non-printable characters", field);
            throw new ParserException(field + " contains non-printable characters");
        }

        // check for a laundry list of "reserved" characters
        if(StringUtils.containsAny(res, "'\"`~!@#$%^&*()[]{}<>:;|\\")) {
            LOG.error("Error {} contains a reserved character", field);
            throw new ParserException(field + " contains a reserved character");
        }

        return res;

    }
}
