package uk.gov.hmcts.reform.civil.aspect;

import feign.Request;
import feign.Response;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorDecoderTelemetryAspectTest {

    @Mock
    private FeignErrorTelemetryService telemetryService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Test
    void shouldTrackTelemetryForDecodedException() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryService);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(504, request, Collections.emptyMap());
        Exception decodedException = new IllegalStateException("decoded");
        when(proceedingJoinPoint.proceed()).thenReturn(decodedException);

        Object result = aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "ClaimStoreApi#getClaimsForClaimant", response);

        assertThat(result).isSameAs(decodedException);
        verify(telemetryService).trackErrorClassification(eq("ClaimStoreApi#getClaimsForClaimant"), same(response), same(decodedException));
    }

    @Test
    void shouldTrackAndRethrowWhenDecodeThrows() throws Throwable {
        ErrorDecoderTelemetryAspect aspect = new ErrorDecoderTelemetryAspect(telemetryService);
        Request request = request(Request.HttpMethod.POST);
        Response response = response(503, request, Collections.emptyMap());
        RuntimeException thrown = new RuntimeException("decode failed");
        when(proceedingJoinPoint.proceed()).thenThrow(thrown);

        assertThatThrownBy(() -> aspect.trackErrorDecoderTelemetry(proceedingJoinPoint, "AnyApi#anyMethod", response))
            .isSameAs(thrown);

        verify(telemetryService).trackErrorClassification(eq("AnyApi#anyMethod"), same(response), same(thrown));
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

    private Response response(int status, Request request, java.util.Map<String, java.util.Collection<String>> headers) {
        return Response.builder()
            .status(status)
            .request(request)
            .headers(headers)
            .build();
    }
}
