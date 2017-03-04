package com.metrink.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.metrink.aggregation.OneMinuteAggregator;
import com.metrink.alert.AlertEngine;
import com.metrink.alert.AlertTablePoller;
import com.metrink.metric.Metric;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.metric.io.MetricReader;
import com.metrink.parser.Parser;
import com.metrink.parser.ParserException;

/**
 * The POST resource for consuming metrics.
 */
@Path("/")
@RequestScoped
public class MetricApi {
    public static final Logger LOG = LoggerFactory.getLogger(MetricApi.class);

    private final Parser parser;
    private final OneMinuteAggregator aggregator;
    private final MetricMetadata metricMetadata;
    private final MetricReader metricReader;
    private final AlertEngine alertEngine;
    private final AlertTablePoller alertTablePoller;

    @Inject
    public MetricApi(final Parser parser,
                     final OneMinuteAggregator aggregator,
                     final MetricMetadata metricMetadata,
                     final MetricReader metricReader,
                     final AlertEngine alertEngine,
                     final AlertTablePoller alertTablePoller) {
        this.parser = parser;
        this.aggregator = aggregator;
        this.metricMetadata = metricMetadata;
        this.metricReader = metricReader;
        this.alertEngine = alertEngine;
        this.alertTablePoller = alertTablePoller;
    }

    public static String convertBodyToString(final byte[] body) {
        try {
            final GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(body));
            return IOUtils.toString(gzip);
        } catch (final IOException e1) {
            try {
                return new String(body, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Cannot read body of POST");
                throw new WebApplicationException(Response.serverError().entity("Cannot read POST body").build());
            }
        }
    }

    @POST
    @Path("api")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response readMetrics(final byte[] body, @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final String bodyAsString = convertBodyToString(body);

        // parse out the metrics
        boolean metricsRemoved = false;
        try {
            // we need to change this method so it takes a string
            final List<Metric> metrics = parser.parse(bodyAsString.getBytes());

            // write the metric to the aggregator
            aggregator.readMetrics(metrics);

            // send the metrics to the alert engine
            alertEngine.processMetrics(metrics);

        } catch (final ParserException e) {
            LOG.error("Parse error reading metrics: {}", e.getMessage(), e);
            throw new WebApplicationException(e);
        } catch (final Exception e) {
            LOG.error("Error reading metrics: {}", e.getMessage(), e);
            throw new WebApplicationException(e);
        }

        final ResponseBuilder response = Response.ok();

        // add the warning header if metrics were removed
        if (metricsRemoved) {
            response.header("Warning", "199 Metrics were discarded because you are over your quota.");
        }

        return response.build();
    }
}
