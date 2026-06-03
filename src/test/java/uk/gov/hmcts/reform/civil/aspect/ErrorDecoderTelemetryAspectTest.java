package uk.gov.hmcts.reform.civil.aspect;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorDecoderTelemetryAspectTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    void shouldTrackTelemetryForDecodedException() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryClient);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(504, request, Collections.emptyMap());
        Exception decodedException = new RetryableException(504, "retry", Request.HttpMethod.GET, null, (Long) null, request);
        when(proceedingJoinPoint.proceed()).thenReturn(decodedException);

        Object result = aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "ClaimStoreApi#getClaimsForClaimant", response);

        assertThat(result).isSameAs(decodedException);
        verify(telemetryClient).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("methodKey", "ClaimStoreApi#getClaimsForClaimant")
            .containsEntry("httpMethod", "GET")
            .containsEntry("status", "504")
            .containsEntry("retryable", "true")
            .containsEntry("defaultException", "RetryableException");
    }

    @Test
    void shouldNotTrackWhenTelemetryClientIsNull() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(null);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(500, request, Collections.emptyMap());
        Exception decodedException = new IllegalStateException("boom");
        when(proceedingJoinPoint.proceed()).thenReturn(decodedException);

        Object result = aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "AnyApi#anyMethod", response);

        assertThat(result).isSameAs(decodedException);
        verify(telemetryClient, never()).trackEvent(eq("httpclient.feign.error.classified"), org.mockito.ArgumentMatchers.any(), isNull());
    }

    @Test
    void shouldTrackAndRethrowWhenDecodeThrows() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryClient);
        Request request = request(Request.HttpMethod.POST);
        Response response = response(503, request, Collections.singletonMap("Retry-After", Collections.singletonList("7")));
        RuntimeException thrown = new RuntimeException("decode failed");
        when(proceedingJoinPoint.proceed()).thenThrow(thrown);

        long beforeDecode = System.currentTimeMillis();
        assertThatThrownBy(() -> aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "AnyApi#anyMethod", response))
            .isSameAs(thrown);
        long afterDecode = System.currentTimeMillis();

        verify(telemetryClient).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture(), isNull());
        long retryAfter = Long.parseLong(propertiesCaptor.getValue().get("retryAfter"));
        assertThat(propertiesCaptor.getValue())
            .containsEntry("idempotent", "false")
            .containsEntry("retryable", "true");
        assertThat(retryAfter).isBetween(beforeDecode + 7000, afterDecode + 7000);
    }

    @Test
    void shouldReturnDecodedExceptionWhenTelemetryTrackingFails() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryClient);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(500, request, Collections.emptyMap());
        Exception decodedException = new IllegalStateException("boom");
        when(proceedingJoinPoint.proceed()).thenReturn(decodedException);
        doThrow(new RuntimeException("telemetry unavailable"))
            .when(telemetryClient).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture(), isNull());

        Object result = aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "AnyApi#anyMethod", response);

        assertThat(result).isSameAs(decodedException);
        assertThat(propertiesCaptor.getValue())
            .containsEntry("methodKey", "AnyApi#anyMethod")
            .containsEntry("status", "500");
    }

    @Test
    void shouldRethrowDecodeExceptionWhenTelemetryTrackingFails() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryClient);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(500, request, Collections.emptyMap());
        RuntimeException thrown = new RuntimeException("decode failed");
        when(proceedingJoinPoint.proceed()).thenThrow(thrown);
        doThrow(new RuntimeException("telemetry unavailable"))
            .when(telemetryClient).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture(), isNull());

        assertThatThrownBy(() -> aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "AnyApi#anyMethod", response))
            .isSameAs(thrown);

        assertThat(propertiesCaptor.getValue())
            .containsEntry("methodKey", "AnyApi#anyMethod")
            .containsEntry("status", "500");
    }

    private Request request(Request.HttpMethod method) {
        return Request.create(
            method,
            "http://test-service/api",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null);
    }

    private Response response(int status, Request request, Map<String, java.util.Collection<String>> headers) {
        return Response.builder()
            .status(status)
            .request(request)
            .headers(headers)
            .build();
    }
}
