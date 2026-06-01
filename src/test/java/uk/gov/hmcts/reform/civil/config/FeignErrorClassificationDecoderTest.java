package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignErrorClassificationDecoderTest {

    private static final String FEIGN_ERROR_CLASSIFIED_EVENT = "httpclient.feign.error.classified";

    @Mock
    private TelemetryClient telemetryClient;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    @Test
    void shouldReturnDefaultFeignExceptionWithoutChangingBehaviour() {
        FeignErrorClassificationDecoder decoder = new FeignErrorClassificationDecoder(telemetryClient, true, true);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(504, request, Collections.emptyMap());

        Exception result = decoder.decode("CoreCaseDataApi#getCase", response);

        assertThat(result)
            .isInstanceOf(FeignException.class)
            .hasMessageContaining("[504]");
        verify(telemetryClient).trackEvent(eq(FEIGN_ERROR_CLASSIFIED_EVENT), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("methodKey", "CoreCaseDataApi#getCase")
            .containsEntry("httpMethod", "GET")
            .containsEntry("status", "504")
            .containsEntry("idempotent", "true")
            .containsEntry("retryable", "true")
            .containsEntry("retryAfterSeconds", "none")
            .containsEntry("defaultException", "GatewayTimeout")
            .containsEntry("handling", "default")
            .containsEntry("feignStatus", "504")
            .containsEntry("retryAfterHeader", "");
    }

    @Test
    void shouldHandleRetryAfterSecondsHeader() {
        FeignErrorClassificationDecoder decoder = new FeignErrorClassificationDecoder(telemetryClient, true, true);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(503, request, Map.of("Retry-After", Collections.singletonList("7")));

        Exception result = decoder.decode("CoreCaseDataApi#getCase", response);

        assertThat(result).isInstanceOf(FeignException.class);
        verify(telemetryClient).trackEvent(eq(FEIGN_ERROR_CLASSIFIED_EVENT), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("status", "503")
            .containsEntry("retryable", "true")
            .containsEntry("retryAfterSeconds", "7")
            .containsEntry("retryAfterHeader", "7");
    }

    @Test
    void shouldHandleRetryAfterDateHeader() {
        FeignErrorClassificationDecoder decoder = new FeignErrorClassificationDecoder(telemetryClient, true, true);
        Request request = request(Request.HttpMethod.GET);
        String retryAfter = Instant.now().plusSeconds(60).toString();
        Response response = response(503, request, Map.of("Retry-After", Collections.singletonList(retryAfter)));

        Exception result = decoder.decode("CoreCaseDataApi#getCase", response);

        assertThat(result).isInstanceOf(FeignException.class);
        verify(telemetryClient).trackEvent(eq(FEIGN_ERROR_CLASSIFIED_EVENT), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue())
            .containsEntry("status", "503")
            .containsEntry("retryable", "true")
            .containsEntry("retryAfterHeader", retryAfter);
        assertThat(Long.parseLong(propertiesCaptor.getValue().get("retryAfterSeconds"))).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void shouldNotFailWhenClassifierIsDisabled() {
        FeignErrorClassificationDecoder decoder = new FeignErrorClassificationDecoder(telemetryClient, false, false);
        Request request = request(Request.HttpMethod.POST);
        Response response = response(429, request, Collections.emptyMap());

        Exception result = decoder.decode("ClaimStoreApi#create", response);

        assertThat(result).isInstanceOf(FeignException.class);
        verify(telemetryClient, never()).trackEvent(eq(FEIGN_ERROR_CLASSIFIED_EVENT), notNull(), isNull());
    }

    @Test
    void shouldHandleNullTelemetryClientGracefully() {
        FeignErrorClassificationDecoder decoder = new FeignErrorClassificationDecoder(null, true, false);
        Request request = request(Request.HttpMethod.GET);
        Response response = response(500, request, Collections.emptyMap());

        Exception result = decoder.decode("AnyApi#anyMethod", response);

        assertThat(result).isNotNull();
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
