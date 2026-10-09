package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HttpClientFeignConfiguration {

    @Value("${http.client.connectTimeout:5000}")
    private int connectTimeout;
    @Value("${http.client.requestTimeout:10000}")
    private int requestTimeout;
    @Value("${http.client.readTimeout:30000}")
    private int readTimeout;

    @Value("${http.client.maxPerRoute:5}")
    private int maxPerRoute;
    @Value("${http.client.maxTotal:25}")
    private int maxTotal;

    @Bean
    public PoolingHttpClientConnectionManager connectionManager4() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        return connectionManager;
    }

    /**
     * Feign's own default decoder, registered as a bean so that every client shares it and
     * {@code ErrorDecoderTelemetryAspect} can advise it. Spring AOP only sees Spring beans:
     * when Feign instantiates {@code ErrorDecoder.Default} internally (which it does once
     * Holunda's global decoder is disabled) the aspect never fires and the
     * {@code httpclient.feign.error.classified} event is never emitted. Behaviour of the
     * clients is unchanged; this is the decoder they would use anyway.
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new ErrorDecoder.Default();
    }

    /**
     * Retry on transient network failures for every Feign client.
     *
     * <p>Holunda's {@code FeignClientConfiguration} contributed exactly this bean, a no-arg
     * {@code Retryer.Default}, and because Spring Cloud OpenFeign declares its own
     * {@code @ConditionalOnMissingBean}, that single bean set the retry behaviour of every Feign
     * client in the service, not just the Camunda ones. Removing the dependency in DTSCCI-6513
     * would otherwise drop all of them to {@code Retryer.NEVER_RETRY}.
     *
     * <p>That matters because Feign raises a {@code RetryableException} for exactly the failures
     * worth retrying: a connection reset, or a pooled keep-alive socket the far side has closed,
     * which surfaces as {@code NoHttpResponseException}. With no retryer those become hard
     * failures on the first attempt. It was caught by the Pact consumer tests, where a mock server
     * restarting on a fixed port leaves a stale pooled connection, but the production surface is
     * every outbound call to CCD, IDAM, CDAM, ref data, HMC and payments.
     *
     * <p>Declared here rather than left implicit so the behaviour is visible and owned. Defaults
     * are Feign's: 5 attempts, 100ms initial interval, 1s maximum.</p>
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default();
    }

    @Bean
    public Client getFeignHttpClient(PoolingHttpClientConnectionManager connectionManager, TelemetryClient telemetryClient,
                                     @Value("${http.client.threshold:15000}") long slowRequestThreshold) {
        return new InstrumentedFeignClient(
            new ApacheHttpClient(getHttpClient(connectionManager)),
            telemetryClient,
            slowRequestThreshold
        );
    }

    private CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(connectTimeout)
            .setConnectionRequestTimeout(requestTimeout)
            .setSocketTimeout(readTimeout).build();

        return org.apache.http.impl.client.HttpClientBuilder.create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(connectionManager).build();
    }

    private record InstrumentedFeignClient(Client delegate, TelemetryClient telemetryClient,
                                           long slowRequestThreshold) implements Client {

        private static final String UNKNOWN_SERVICE = "unknown";

        private static Map<String, String> buildProperties(String service, long duration, boolean success) {
            Map<String, String> properties = new HashMap<>();
            properties.put("service", service);
            properties.put("durationMs", String.valueOf(duration));
            properties.put("success", String.valueOf(success));
            return properties;
        }

        private static boolean isConnectionPoolTimeout(Exception e) {
            return e != null && ((e.getMessage() != null
                && e.getMessage().contains("Timeout waiting for connection from pool"))
                || e.getClass().getName().contains("ConnectionPoolTimeoutException"));
        }

        private static String extractService(URI uri) {
            return uri != null && uri.getHost() != null ? uri.getHost() : UNKNOWN_SERVICE;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            long startTime = System.currentTimeMillis();

            String service;
            try {
                service = extractService(URI.create(request.url()));
            } catch (Exception e) {
                service = UNKNOWN_SERVICE;
            }

            try {
                Response response = delegate.execute(request, options);
                reportMetrics(service, System.currentTimeMillis() - startTime, true, null);
                return response;
            } catch (Exception e) {
                reportMetrics(service, System.currentTimeMillis() - startTime, false, e);
                throw e;
            }
        }

        private void reportMetrics(String service, long duration, boolean success, Exception e) {
            if (telemetryClient == null) {
                return;
            }
            telemetryClient.trackMetric("httpclient.request.duration_ms", duration);

            if (duration >= slowRequestThreshold) {
                telemetryClient.trackEvent(
                    "httpclient.slow_request",
                    buildProperties(service, duration, success),
                    null);
            }

            if (!success && isConnectionPoolTimeout(e)) {
                telemetryClient.trackMetric("httpclient.pool.timeout.count", 1.0);
                telemetryClient.trackEvent(
                    "httpclient.pool.timeout",
                    buildProperties(service, duration, false),
                    null);
            }
        }
    }
}
