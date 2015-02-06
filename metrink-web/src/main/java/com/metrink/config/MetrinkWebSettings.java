package com.metrink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metrink.croquet.WicketSettings;

/**
 * Master configuration for the Metrink Web project.
 * This may contain project specific settings. All parameters should be
 * defaulted to some sane value or an IllegalStateException should be thrown when an access occurs.
 */
public class MetrinkWebSettings extends WicketSettings implements MetrinkSettings {
    private static final long serialVersionUID = 1L;

    // The technology specific naming as there isn't a ODBC for no-SQL that makes them easily swappable.
    @JsonProperty("cassandra")
    private CassandraSettings cassandra = new CassandraSettings();

    @JsonProperty("email")
    private EmailSettings emailSettings = new EmailSettings();

    /**
     * Get cassandra.
     * @return the cassandra
     */
    @Override
    public CassandraSettings getCassandraSettings() {
        return cassandra;
    }

    /**
     * Set cassandra.
     * @param cassandra the cassandra to set
     */
    @Override
    public void setCassandraSettings(final CassandraSettings cassandra) {
        this.cassandra = cassandra;
    }

    @Override
    public EmailSettings getEmailSettings() {
        return emailSettings;
    }

    @Override
    public void setEmailSettings(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }
}
