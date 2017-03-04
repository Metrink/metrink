package com.metrink.config;


public interface MetrinkSettings {

    public CassandraSettings getCassandraSettings();

    public void setCassandraSettings(final CassandraSettings cassandra);

    public EmailSettings getEmailSettings();

    public void setEmailSettings(final EmailSettings emailSettings);
}
