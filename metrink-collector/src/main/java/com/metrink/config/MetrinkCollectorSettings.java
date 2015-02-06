package com.metrink.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metrink.croquet.RestSettings;

public class MetrinkCollectorSettings extends RestSettings implements MetrinkSettings {

    private static final long serialVersionUID = -7148175508311371048L;

    @JsonProperty("retention_days")
    private Integer retentionDays = 90;

    // The technology specific naming as there isn't a ODBC for no-SQL that makes them easily swappable.
    @JsonProperty("cassandra")
    private CassandraSettings cassandraSettings = new CassandraSettings();

    @JsonProperty("email")
    private EmailSettings emailSettings = new EmailSettings();

    /**
     * Get cassandra.
     * @return the cassandra
     */
    @Override
    public CassandraSettings getCassandraSettings() {
        return cassandraSettings;
    }

    /**
     * Set cassandra.
     * @param cassandra the cassandra to set
     */
    @Override
    public void setCassandraSettings(final CassandraSettings cassandra) {
        this.cassandraSettings = cassandra;
    }

    @Override
    public EmailSettings getEmailSettings() {
        return emailSettings;
    }

    @Override
    public void setEmailSettings(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

}
