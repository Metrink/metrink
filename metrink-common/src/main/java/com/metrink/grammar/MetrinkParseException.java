package com.metrink.grammar;

public class MetrinkParseException extends Exception {

    private static final long serialVersionUID = 1L;

    public MetrinkParseException(String message) {
        super(message);
    }

    public MetrinkParseException(Exception e) {
        super(e);
    }

    public MetrinkParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
