package uk.gov.hmcts.reform.civil.service;

import feign.Request;
import feign.Response;
import feign.RetryableException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignErrorTelemetryServiceTest {

    @Mock
    private TelemetryService telemetryService;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    void shouldTrackTelemetryForRetryableException() {
        FeignErrorTelemetryService service = new FeignErrorTelemetryService(telemetryService);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(504, request, Collections.emptyMap());
        RetryableException exception = new RetryableException(504, "retry", Request.HttpMethod.GET, null, (Long) null, request);

        service.trackErrorClassification("ClaimStoreApi#getClaimsForClaimant", response, exception);

        verify(telemetryService).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("methodKey", "ClaimStoreApi#getClaimsForClaimant")
            .containsEntry("httpMethod", "GET")
            .containsEntry("status", "504")
            .containsEntry("retryable", "true")
            .containsEntry("defaultException", "RetryableException");
    }

    @Test
    void shouldTrackRetryAfterClassificationForThrownException() {
        FeignErrorTelemetryService service = new FeignErrorTelemetryService(telemetryService);
        Request request = request(Request.HttpMethod.POST);
        Response response = response(503, request, Collections.singletonMap("Retry-After", Collections.singletonList("7")));
        RuntimeException thrown = new RuntimeException("decode failed");

        long beforeTrack = System.currentTimeMillis();
        service.trackErrorClassification("AnyApi#anyMethod", response, thrown);
        long afterTrack = System.currentTimeMillis();

        verify(telemetryService).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture());
        long retryAfter = Long.parseLong(propertiesCaptor.getValue().get("retryAfter"));
        assertThat(retryAfter).isBetween(beforeTrack + 7000, afterTrack + 7000);
        assertThat(propertiesCaptor.getValue())
            .containsEntry("idempotent", "false")
            .containsEntry("retryable", "true");
    }

    @Test
    void shouldSwallowTelemetryTrackingFailure() {
        FeignErrorTelemetryService service = new FeignErrorTelemetryService(telemetryService);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(500, request, Collections.emptyMap());
        doThrow(new RuntimeException("telemetry unavailable"))
            .when(telemetryService).trackEvent(eq("httpclient.feign.error.classified"), propertiesCaptor.capture());

        service.trackErrorClassification("AnyApi#anyMethod", response, new IllegalStateException("boom"));

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
            null
        );
    }

    private Response response(int status, Request request, Map<String, java.util.Collection<String>> headers) {
        return Response.builder()
            .status(status)
            .request(request)
            .headers(headers)
            .build();
    }
}
