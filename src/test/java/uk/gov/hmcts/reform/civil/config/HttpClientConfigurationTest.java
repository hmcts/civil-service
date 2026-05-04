package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Client;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpClientConfigurationTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    void shouldTrackTimeoutEventWithServiceContext() throws Exception {
        Object feignClient = newInnerInstance(
            new Class<?>[]{feign.Client.class, TelemetryClient.class, long.class},
            new Object[]{null, telemetryClient, 15000L});

        invokeReportMetrics(feignClient,
                            new RuntimeException("Timeout waiting for connection from pool"));

        verify(telemetryClient).trackMetric("httpclient.request.duration_ms", 30000.0);
        verify(telemetryClient).trackMetric("httpclient.pool.timeout.count", 1.0);
        verify(telemetryClient).trackEvent(eq("httpclient.pool.timeout"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("service", "hmc")
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
        RestTemplate restTemplate = configuration.restTemplate();
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
        assertThat(restTemplate.getInterceptors()).isEmpty();
    }

    @Test
    void shouldCreateConnectionManager4() {
        HttpClientConfiguration configuration = new HttpClientConfiguration(telemetryClient);
        org.apache.http.impl.conn.PoolingHttpClientConnectionManager cm = configuration.connectionManager4();
        assertThat(cm).isNotNull();
    }

    @Test
    void shouldExtractServiceFromUri() throws Exception {
        Class<?> innerClass = Class.forName("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient");
        String service = ReflectionTestUtils.invokeMethod(innerClass, "extractService", URI.create("http://ccd-data-store-api/cases"));
        assertThat(service).isEqualTo("ccd-data-store-api");
    }

    @Test
    void shouldReturnUnknownServiceWhenUriIsNull() throws Exception {
        Class<?> innerClass = Class.forName("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient");
        String service = ReflectionTestUtils.invokeMethod(innerClass, "extractService", (URI) null);
        assertThat(service).isEqualTo("unknown");
    }

    @Test
    void shouldReturnUnknownServiceWhenHostIsNull() throws Exception {
        Class<?> innerClass = Class.forName("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient");
        String service = ReflectionTestUtils.invokeMethod(innerClass, "extractService", URI.create("path"));
        assertThat(service).isEqualTo("unknown");
    }

    @Test
    void shouldDetectConnectionPoolTimeout() throws Exception {
        Class<?> innerClass = Class.forName("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient");
        Exception e1 = new RuntimeException("Timeout waiting for connection from pool");
        boolean result1 = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
            innerClass,
            "isConnectionPoolTimeout",
            e1
        ));
        assertThat(result1).isTrue();

        Exception e2 = new RuntimeException("other error");
        boolean result2 = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
            innerClass,
            "isConnectionPoolTimeout",
            e2
        ));
        assertThat(result2).isFalse();

        boolean result3 = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
            innerClass,
            "isConnectionPoolTimeout",
            (Exception) null
        ));
        assertThat(result3).isFalse();
    }

    @Test
    void shouldExecuteInstrumentedFeignClient() throws Exception {
        Client delegate = mock(Client.class);
        Object feignClient = newInnerInstance(
            new Class<?>[]{Client.class, TelemetryClient.class, long.class},
            new Object[]{delegate, telemetryClient, 15000L});

        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://test-service/api",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null);

        Response response = Response.builder().request(request).status(200).build();
        when(delegate.execute(any(), any())).thenReturn(response);

        try (Response result = ((Client) feignClient).execute(request, new Request.Options())) {
            assertThat(result).isEqualTo(response);
            verify(telemetryClient).trackMetric(eq("httpclient.request.duration_ms"), anyDouble());
        }
    }

    @Test
    void shouldExecuteInstrumentedFeignClientOnException() throws Exception {
        Client delegate = mock(Client.class);
        Object feignClient = newInnerInstance(
            new Class<?>[]{Client.class, TelemetryClient.class, long.class},
            new Object[]{delegate, telemetryClient, 15000L});

        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://test-service/api",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null);

        IOException exception = new IOException("error");
        when(delegate.execute(any(), any())).thenThrow(exception);

        assertThatThrownBy(() -> ((Client) feignClient).execute(request, new Request.Options()))
            .isEqualTo(exception);

        verify(telemetryClient).trackMetric(eq("httpclient.request.duration_ms"), anyDouble());
    }

    private Object newInnerInstance(Class<?>[] parameterTypes, Object[] args) throws Exception {
        Constructor<?> constructor =
            Class.forName("uk.gov.hmcts.reform.civil.config.HttpClientConfiguration$InstrumentedFeignClient")
                .getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private void invokeReportMetrics(Object target, Exception exception) throws Exception {
        Method method = target.getClass().getDeclaredMethod("reportMetrics",
            String.class, long.class, boolean.class, Exception.class);
        method.setAccessible(true);
        method.invoke(target, "hmc", 30000L, false, exception);
    }
}
