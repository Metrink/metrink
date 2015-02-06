package com.metrink.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cassandra configuration options.
 */
public class CassandraSettings {

    @JsonProperty("cluster_name")
    private String clusterName = "Test Cluster";

    @JsonProperty("keyspace")
    private String keyspace = "metrink";

    @JsonProperty("column_family")
    private String columnFamily = "metrics";

    @JsonProperty("connection_pool")
    private String connectionPool = "MetrinkConnectionPool";

    @JsonProperty("port")
    private int port = 9160;

    @JsonProperty("max_connections_per_host")
    private int maxConnectionsPerHost = 1;

    @JsonProperty("seed")
    private String seed = "127.0.0.1:9160";

    /**
     * Get clusterName.
     * @return the clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Set clusterName.
     * @param clusterName the clusterName to set
     */
    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Get keyspace.
     * @return the keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Set keyspace.
     * @param keyspace the keyspace to set
     */
    public void setKeyspace(final String keyspace) {
        this.keyspace = keyspace;
    }

    /**
     * Get columnFamily.
     * @return the columnFamily
     */
    public String getColumnFamily() {
        return columnFamily;
    }

    /**
     * Set columnFamily.
     * @param columnFamily the columnFamily to set
     */
    public void setColumnFamily(final String columnFamily) {
        this.columnFamily = columnFamily;
    }

    /**
     * Get connectionPool.
     * @return the connectionPool
     */
    public String getConnectionPool() {
        return connectionPool;
    }

    /**
     * Set connectionPool.
     * @param connectionPool the connectionPool to set
     */
    public void setConnectionPool(final String connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Get port.
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set port.
     * @param port the port to set
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Get maxConnectionsPerHost.
     * @return the maxConnectionsPerHost
     */
    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    /**
     * Set maxConnectionsPerHost.
     * @param maxConnectionsPerHost the maxConnectionsPerHost to set
     */
    public void setMaxConnectionsPerHost(final int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * Get seed.
     * @return the seed
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Set seed.
     * @param seed the seed to set
     */
    public void setSeed(final String seed) {
        this.seed = seed;
    }
}
