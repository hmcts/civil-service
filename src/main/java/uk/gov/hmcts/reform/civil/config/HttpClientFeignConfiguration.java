package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("###### Creating Pooling Http Client Connection Manager ######");
        log.info("Max Pre Route is {}, Max total is {}", maxPerRoute, maxTotal);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        return connectionManager;
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
        log.info("###### Creating HTTP client with connection pool ######");
        log.info("Connection timeout is {}, Request timeout is {}, Read timeout is {}", connectTimeout, requestTimeout, readTimeout);
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
