package uk.gov.hmcts.reform.civil.aspect;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = ErrorDecoderTelemetryAspectIntegrationTest.TestConfig.class)
@SuppressWarnings({"java:S6813", "java:S5960"})
class ErrorDecoderTelemetryAspectIntegrationTest {

    public static final String METHOD_KEY = "TestClient#getAnything";
    public static final String DECODED = "decoded";

    @MockBean
    private FeignErrorTelemetryService telemetryService;

    @Autowired
    private ErrorDecoder errorDecoder;

    @Test
    void shouldTrackTelemetryWhenSpringManagedErrorDecoderDecodesResponse() {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://test-service/api",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response response = Response.builder()
            .status(504)
            .request(request)
            .headers(Collections.emptyMap())
            .build();

        Exception result = errorDecoder.decode(METHOD_KEY, response);

        assertThat(result)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(DECODED);
        verify(telemetryService).trackErrorClassification(
            eq(METHOD_KEY),
            eq(response),
            argThat(throwable -> throwable instanceof IllegalStateException
                && DECODED.equals(throwable.getMessage()))
        );
    }

    @Configuration
    @EnableAspectJAutoProxy
    @Import(ErrorDecoderTelemetryAspect.class)
    static class TestConfig {

        @Bean
        ErrorDecoder errorDecoder() {
            return (methodKey, response) -> new IllegalStateException(DECODED);
        }
    }

}
