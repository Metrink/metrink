package com.metrink.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailSettings {

    @JsonProperty(value="server", required=true)
    private String server;

    @JsonProperty("ssl_port")
    private String sslPort = "465";

    @JsonProperty("user")
    private String user;

    @JsonProperty("pass")
    private String pass;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getSslPort() {
        return sslPort;
    }

    public void setSslPort(String sslPort) {
        this.sslPort = sslPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

}
