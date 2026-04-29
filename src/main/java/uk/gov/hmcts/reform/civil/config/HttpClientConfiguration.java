package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfiguration {

    private static final String UNKNOWN_SERVICE = "unknown";

    @Value("${http.client.readTimeout}")
    private int readTimeout;

    @Value("${http.client.threshold:15000}")
    private long slowRequestThreshold;

    private final TelemetryClient telemetryClient;

    @Bean
    public Client getFeignHttpClient(PoolingHttpClientConnectionManager connectionManager) {
        return new InstrumentedFeignClient(new ApacheHttpClient(getHttpClient(connectionManager)), telemetryClient, slowRequestThreshold);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getRestTemplateHttpClient()));
        return restTemplate;
    }

    @Bean
    public PoolingHttpClientConnectionManager connectionManager4() {
        return new PoolingHttpClientConnectionManager();
    }

    private HttpClient getRestTemplateHttpClient() {
        final RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(readTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        return HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create().build())
            .build();
    }

    private CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager) {
        org.apache.http.client.config.RequestConfig config = org.apache.http.client.config.RequestConfig.custom()
            .setConnectTimeout(readTimeout)
            .setConnectionRequestTimeout(readTimeout)
            .setSocketTimeout(readTimeout)
            .build();

        return org.apache.http.impl.client.HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(connectionManager)
            .build();
    }

    @RequiredArgsConstructor
    private static class InstrumentedFeignClient implements Client {
        private final Client delegate;
        private final TelemetryClient telemetryClient;
        private final long slowRequestThreshold;

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            long startTime = System.currentTimeMillis();

            String service;
            try {
                service =  extractService(URI.create(request.url()));
            } catch (Exception e) {
                service =  UNKNOWN_SERVICE;
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
            telemetryClient.trackMetric("httpclient.request.duration_ms", duration);

            if (duration >= slowRequestThreshold) {
                // Emit only slow calls so the telemetry stays useful in production.
                telemetryClient.trackEvent("httpclient.slow_request", buildProperties(service, duration, success), null);
            }

            if (!success && isConnectionPoolTimeout(e)) {
                telemetryClient.trackMetric("httpclient.pool.timeout.count", 1.0);
                telemetryClient.trackEvent("httpclient.pool.timeout",
                                           buildProperties(service, duration, false),
                                           null);
            }
        }

        private static Map<String, String> buildProperties(String service, long duration, boolean success) {
            Map<String, String> properties = new HashMap<>();
            properties.put("service", service);
            properties.put("durationMs", String.valueOf(duration));
            properties.put("success", String.valueOf(success));
            return properties;
        }

        private static boolean isConnectionPoolTimeout(Exception e) {
            return e != null && ((e.getMessage() != null && e.getMessage().contains("Timeout waiting for connection from pool"))
                || e.getClass().getName().contains("ConnectionPoolTimeoutException"));
        }

        private static String extractService(URI uri) {
            return uri != null && uri.getHost() != null ? uri.getHost() : UNKNOWN_SERVICE;
        }
    }

}
