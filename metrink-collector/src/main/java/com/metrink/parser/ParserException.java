package com.metrink.parser;

import java.text.MessageFormat;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final Logger LOG = LoggerFactory.getLogger(ParserException.class);

    public ParserException(JSONException e, String field) {
        super(MessageFormat.format("Error getting {0}: {1}", field, e.getMessage()));
    }

    public ParserException(JSONException e) {
        super(e);
    }

    public ParserException(String error) {
        super(error);
    }
}
