package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpClientConfigurationTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    void shouldTrackSlowRequestEventWithServiceContext() throws Exception {
        Object interceptor = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$RestTemplateMetricsInterceptor");

        invokeReportMetrics(interceptor, "ccd-data-store-api", "RestTemplate", 5000L, true, null);

        verify(telemetryClient).trackMetric("http.client.request.duration_ms", 5000.0);
        verify(telemetryClient).trackEvent(eq("http.client.slow_request"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("service", "ccd-data-store-api")
            .containsEntry("client", "RestTemplate")
            .containsEntry("durationMs", "5000")
            .containsEntry("success", "true");
    }

    @Test
    void shouldNotTrackSlowRequestEventBelowThreshold() throws Exception {
        Object interceptor = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$RestTemplateMetricsInterceptor");

        invokeReportMetrics(interceptor, "ccd-data-store-api", "RestTemplate", 4999L, true, null);

        verify(telemetryClient).trackMetric("http.client.request.duration_ms", 4999.0);
        verify(telemetryClient, never()).trackEvent(eq("http.client.slow_request"), anyMap(), isNull());
    }

    @Test
    void shouldTrackTimeoutEventWithServiceContext() throws Exception {
        Object interceptor = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient",
            new Class<?>[]{feign.Client.class, TelemetryClient.class},
            new Object[]{null, telemetryClient});

        invokeReportMetrics(interceptor, "hmc", "Feign", 30000L, false,
            new RuntimeException("Timeout waiting for connection from pool"));

        verify(telemetryClient).trackMetric("http.client.request.duration_ms", 30000.0);
        verify(telemetryClient).trackMetric("httpclient.pool.timeout.count", 1.0);
        verify(telemetryClient).trackEvent(eq("httpclient.pool.timeout"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("service", "hmc")
            .containsEntry("client", "Feign")
            .containsEntry("durationMs", "30000")
            .containsEntry("success", "false");
    }

    @Test
    void shouldCreateFeignHttpClient() {
        HttpClientConfiguration configuration = new HttpClientConfiguration(telemetryClient);
        org.apache.http.impl.conn.PoolingHttpClientConnectionManager cm = mock(org.apache.http.impl.conn.PoolingHttpClientConnectionManager.class);
        Client client = configuration.getFeignHttpClient(cm);
        assertThat(client).isNotNull();
    }

    @Test
    void shouldCreateRestTemplate() {
        HttpClientConfiguration configuration = new HttpClientConfiguration(telemetryClient);
        PoolingHttpClientConnectionManager cm = mock(PoolingHttpClientConnectionManager.class);
        RestTemplate restTemplate = configuration.restTemplate(cm);
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
        assertThat(restTemplate.getInterceptors()).hasSize(1);
    }

    @Test
    void shouldCreateConnectionManager5() {
        HttpClientConfiguration configuration = new HttpClientConfiguration(telemetryClient);
        ReflectionTestUtils.setField(configuration, "readTimeout", 5000);
        PoolingHttpClientConnectionManager cm = configuration.connectionManager5();
        assertThat(cm).isNotNull();
    }

    @Test
    void shouldCreateConnectionManager4() {
        HttpClientConfiguration configuration = new HttpClientConfiguration(telemetryClient);
        org.apache.http.impl.conn.PoolingHttpClientConnectionManager cm = configuration.connectionManager4();
        assertThat(cm).isNotNull();
    }

    @Test
    void shouldExtractServiceFromUri() {
        assertThat(HttpClientConfiguration.class).hasDeclaredMethods("extractService");
        String service = (String) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "extractService", URI.create("http://ccd-data-store-api/cases"));
        assertThat(service).isEqualTo("ccd-data-store-api");
    }

    @Test
    void shouldReturnUnknownServiceWhenUriIsNull() {
        String service = (String) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "extractService", (URI) null);
        assertThat(service).isEqualTo("unknown");
    }

    @Test
    void shouldReturnUnknownServiceWhenHostIsNull() {
        String service = (String) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "extractService", URI.create("path"));
        assertThat(service).isEqualTo("unknown");
    }

    @Test
    void shouldDetectConnectionPoolTimeout() {
        Exception e1 = new RuntimeException("Timeout waiting for connection from pool");
        boolean result1 = (boolean) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "isConnectionPoolTimeout", e1);
        assertThat(result1).isTrue();

        Exception e2 = new RuntimeException("other error");
        boolean result2 = (boolean) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "isConnectionPoolTimeout", e2);
        assertThat(result2).isFalse();

        boolean result3 = (boolean) ReflectionTestUtils.invokeMethod(HttpClientConfiguration.class, "isConnectionPoolTimeout", (Exception) null);
        assertThat(result3).isFalse();
    }

    @Test
    void shouldInterceptRestTemplateRequest() throws Exception {
        Object interceptor = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$RestTemplateMetricsInterceptor");

        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("http://test-service/api"));
        byte[] body = "body".getBytes();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        ClientHttpResponse result = ((org.springframework.http.client.ClientHttpRequestInterceptor) interceptor)
            .intercept(request, body, execution);

        assertThat(result).isEqualTo(response);
        verify(telemetryClient).trackMetric(eq("http.client.request.duration_ms"), anyDouble());
    }

    @Test
    void shouldInterceptRestTemplateRequestOnException() throws Exception {
        Object interceptor = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$RestTemplateMetricsInterceptor");

        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("http://test-service/api"));
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        IOException exception = new IOException("error");
        when(execution.execute(any(), any())).thenThrow(exception);

        assertThatThrownBy(() -> ((org.springframework.http.client.ClientHttpRequestInterceptor) interceptor)
            .intercept(request, "body".getBytes(), execution))
            .isEqualTo(exception);

        verify(telemetryClient).trackMetric(eq("http.client.request.duration_ms"), anyDouble());
    }

    @Test
    void shouldExecuteInstrumentedFeignClient() throws Exception {
        Client delegate = mock(Client.class);
        Object feignClient = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient",
            new Class<?>[]{Client.class, TelemetryClient.class},
            new Object[]{delegate, telemetryClient});

        Request request = Request.create(Request.HttpMethod.GET, "http://test-service/api", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().request(request).status(200).build();
        when(delegate.execute(any(), any())).thenReturn(response);

        Response result = ((Client) feignClient).execute(request, new Request.Options());

        assertThat(result).isEqualTo(response);
        verify(telemetryClient).trackMetric(eq("http.client.request.duration_ms"), anyDouble());
    }

    @Test
    void shouldExecuteInstrumentedFeignClientOnException() throws Exception {
        Client delegate = mock(Client.class);
        Object feignClient = newInnerInstance("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient",
            new Class<?>[]{Client.class, TelemetryClient.class},
            new Object[]{delegate, telemetryClient});

        Request request = Request.create(Request.HttpMethod.GET, "http://test-service/api", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        IOException exception = new IOException("error");
        when(delegate.execute(any(), any())).thenThrow(exception);

        assertThatThrownBy(() -> ((Client) feignClient).execute(request, new Request.Options()))
            .isEqualTo(exception);

        verify(telemetryClient).trackMetric(eq("http.client.request.duration_ms"), anyDouble());
    }

    private Object newInnerInstance(String className) throws Exception {
        return newInnerInstance(className, new Class<?>[]{TelemetryClient.class}, new Object[]{telemetryClient});
    }

    private Object newInnerInstance(String className, Class<?>[] parameterTypes, Object[] args) throws Exception {
        Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private void invokeReportMetrics(Object target, String service, String client, long duration, boolean success,
                                     Exception exception) throws Exception {
        Method method = target.getClass().getDeclaredMethod("reportMetrics",
            String.class, String.class, long.class, boolean.class, Exception.class);
        method.setAccessible(true);
        method.invoke(target, service, client, duration, success, exception);
    }
}
