package com.metrink.inject;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.metrink.config.CassandraSettings;
import com.metrink.config.EmailSettings;
import com.metrink.config.MetrinkSettings;
import com.metrink.grammar.alert.AlertQuery.AlertQueryFactory;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.metric.io.MetricReader;
import com.metrink.metric.io.MetricReaderWriter;
import com.metrink.metric.io.MetricWriter;
import com.metrink.metric.io.impl.CassandraReaderWriter;
import com.metrink.metric.io.impl.CassandraReaderWriter.MetricRowIdSerializer;
import com.metrink.metric.io.impl.CassandraReaderWriter.MetricRowKey;
import com.metrink.metric.io.impl.MySqlMetricMetadata;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.Serializer;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class CommonModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(CommonModule.class);

    private final MetrinkSettings settings;

    public CommonModule(final MetrinkSettings settings) {
        LOG.debug("Constructing common guice module for the collector");

        this.settings = settings;
    }

    @Override
    protected void configure() {
        LOG.debug("Executing configure on common guice module");

        bind(DateTimeProvider.class).toInstance(new DateTimeProvider() {
            @Override
            public DateTime get() {
                return new DateTime();
            }
        });

        // Metric meta-data (i.e., unique devices, groups, owners) is stored in a SQL backend.
        bind(MetricMetadata.class).to(MySqlMetricMetadata.class);

        bind(MetricReaderWriter.class).to(CassandraReaderWriter.class);
        bind(MetricReader.class).to(CassandraReaderWriter.class);
        bind(MetricWriter.class).to(CassandraReaderWriter.class);

        install(new FactoryModuleBuilder().build(AlertQueryFactory.class));
    }

    /**
     * Obtain an unstarted astyanax context for cassandra.
     * @return the configured context
     */
    @Provides
    AstyanaxContext<Keyspace> providesCassandraContext() {
        final CassandraSettings cassandraSettings = settings.getCassandraSettings();

        return new AstyanaxContext.Builder()
            .forCluster(cassandraSettings.getClusterName())
            .forKeyspace(cassandraSettings.getKeyspace())
            .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                .setCqlVersion("3.0.0")
            )
            .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(cassandraSettings.getConnectionPool())
                .setPort(cassandraSettings.getPort())
                .setMaxConnsPerHost(cassandraSettings.getMaxConnectionsPerHost())
                .setSeeds(cassandraSettings.getSeed())
            )
            .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
            .buildKeyspace(ThriftFamilyFactory.getInstance());
    }

    /**
     * Obtain the cassandra serializer used to translate the {@link MetricRowKey}.
     * @return the cassandra metric row id serializer
     */
    @Provides
    Serializer<MetricRowKey> providesMetricsRowKeySerializer() {
        return new MetricRowIdSerializer();
    }

    /**
     * Obtain the metrics column family for cassandra.
     * @param rowSerializer the cassandra metric row id serializer
     * @return the metrics column family
     */
    @Provides @Inject
    ColumnFamily<MetricRowKey, Long> providesMetricsColumnFamily(final Serializer<MetricRowKey> rowSerializer) {

        return new ColumnFamily<MetricRowKey, Long>(
                settings.getCassandraSettings().getColumnFamily(),
                rowSerializer,
                LongSerializer.get());
    }

    /**
     * Configures a SimpleEmail.
     * @return the simple email
     */
    @Provides
    SimpleEmail providesSimpleEmail() {
        final SimpleEmail ret = new SimpleEmail();

        configureEmail(ret, settings.getEmailSettings());

        return ret;
    }

    /**
     * Configures a HtmlEmail.
     * @return the html email
     */
    @Provides
    HtmlEmail providesHtmlEmail() {
        final HtmlEmail ret = new HtmlEmail();
        configureEmail(ret, settings.getEmailSettings());
        return ret;
    }

    /**
     * Helper method used to configure both {@link SimpleEmail} and {@link HtmlEmail}.
     * @param email the email to configure
     */
    private void configureEmail(final Email email, final EmailSettings emailSettings) {
        email.setHostName(emailSettings.getServer());
        email.setSslSmtpPort(emailSettings.getSslPort());
        email.setAuthentication(emailSettings.getUser(), emailSettings.getPass());
        email.setStartTLSEnabled(true);
        email.setSSLOnConnect(true);

        try {
            email.setFrom("no-reply@metrink.com", "Metrink No-Reply");
        } catch (final EmailException e) {
            LOG.error("Error setting from address: {}", e.getMessage());
        }

        email.setBounceAddress("wspeirs@metrink.com");
    }
}
