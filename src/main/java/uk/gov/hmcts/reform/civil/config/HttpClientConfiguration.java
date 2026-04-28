package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfiguration {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 5000L;
    private static final String UNKNOWN_SERVICE = "unknown";

    @Value("${http.client.readTimeout}")
    private int readTimeout;

    private final TelemetryClient telemetryClient;

    @Bean
    public Client getFeignHttpClient(org.apache.http.impl.conn.PoolingHttpClientConnectionManager connectionManager4) {
        return new InstrumentedFeignClient(new ApacheHttpClient(getHttpClient(connectionManager4)), telemetryClient);
    }

    @Bean
    public RestTemplate restTemplate(PoolingHttpClientConnectionManager connectionManager5) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getRestTemplateHttpClient(connectionManager5)));
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateMetricsInterceptor(telemetryClient)));
        return restTemplate;
    }

    @Bean
    public PoolingHttpClientConnectionManager connectionManager5() {
        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(readTimeout))
            .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        return PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultConnectionConfig(connectionConfig)
            .build();
    }

    @Bean
    public org.apache.http.impl.conn.PoolingHttpClientConnectionManager connectionManager4() {
        return new org.apache.http.impl.conn.PoolingHttpClientConnectionManager();
    }

    private HttpClient getRestTemplateHttpClient(PoolingHttpClientConnectionManager connectionManager) {
        final RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(readTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        return HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(connectionManager)
            .build();
    }

    private org.apache.http.impl.client.CloseableHttpClient getHttpClient(org.apache.http.impl.conn.PoolingHttpClientConnectionManager connectionManager) {
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
    private static class RestTemplateMetricsInterceptor implements ClientHttpRequestInterceptor {
        private final TelemetryClient telemetryClient;

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            long startTime = System.currentTimeMillis();
            String service = extractService(request.getURI());
            try {
                ClientHttpResponse response = execution.execute(request, body);
                reportMetrics(service, "RestTemplate", System.currentTimeMillis() - startTime, true, null);
                return response;
            } catch (Exception e) {
                reportMetrics(service, "RestTemplate", System.currentTimeMillis() - startTime, false, e);
                throw e;
            }
        }

        private void reportMetrics(String service, String client, long duration, boolean success, Exception e) {
            telemetryClient.trackMetric("http.client.request.duration_ms", duration);
            reportSlowRequest(telemetryClient, service, client, duration, success);

            if (!success && isConnectionPoolTimeout(e)) {
                telemetryClient.trackMetric("httpclient.pool.timeout.count", 1.0);
                telemetryClient.trackEvent("httpclient.pool.timeout", buildProperties(service, client, duration, success), null);
            }
        }
    }

    @RequiredArgsConstructor
    private static class InstrumentedFeignClient implements Client {
        private final Client delegate;
        private final TelemetryClient telemetryClient;

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
                reportMetrics(service, "Feign", System.currentTimeMillis() - startTime, true, null);
                return response;
            } catch (Exception e) {
                reportMetrics(service, "Feign", System.currentTimeMillis() - startTime, false, e);
                throw e;
            }
        }

        private void reportMetrics(String service, String client, long duration, boolean success, Exception e) {
            telemetryClient.trackMetric("http.client.request.duration_ms", duration);
            reportSlowRequest(telemetryClient, service, client, duration, success);

            if (!success && isConnectionPoolTimeout(e)) {
                telemetryClient.trackMetric("httpclient.pool.timeout.count", 1.0);
                telemetryClient.trackEvent("httpclient.pool.timeout", buildProperties(service, client, duration, success), null);
            }
        }
    }

    private static void reportSlowRequest(TelemetryClient telemetryClient, String service, String client,
                                          long duration, boolean success) {
        if (duration >= SLOW_REQUEST_THRESHOLD_MS) {
            // Emit only slow calls so the telemetry stays useful in production.
            telemetryClient.trackEvent("http.client.slow_request", buildProperties(service, client, duration, success), null);
        }
    }

    private static boolean isConnectionPoolTimeout(Exception e) {
        return e != null && ((e.getMessage() != null && e.getMessage().contains("Timeout waiting for connection from pool"))
            || e.getClass().getName().contains("ConnectionPoolTimeoutException"));
    }

    private static Map<String, String> buildProperties(String service, String client, long duration, boolean success) {
        Map<String, String> properties = new HashMap<>();
        properties.put("service", service);
        properties.put("client", client);
        properties.put("durationMs", String.valueOf(duration));
        properties.put("success", String.valueOf(success));
        return properties;
    }

    private static String extractService(URI uri) {
        return uri != null && uri.getHost() != null ? uri.getHost() : UNKNOWN_SERVICE;
    }

}
