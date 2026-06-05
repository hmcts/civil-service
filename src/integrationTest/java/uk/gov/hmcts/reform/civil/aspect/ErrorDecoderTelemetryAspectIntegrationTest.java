package uk.gov.hmcts.reform.civil.aspect;

import com.microsoft.applicationinsights.TelemetryClient;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = ErrorDecoderTelemetryAspectIntegrationTest.TestConfig.class)
@SuppressWarnings("java:S6813")
class ErrorDecoderTelemetryAspectIntegrationTest {

    @MockBean
    private TelemetryClient telemetryClient;

    @Autowired
    private ErrorDecoder errorDecoder;

    @Test
    @SuppressWarnings("java:S5960")
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

        Exception result = errorDecoder.decode("TestClient#getSomething", response);

        assertThat(result)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("decoded");
        verify(telemetryClient).trackEvent(
            eq("httpclient.feign.error.classified"),
            argThat(properties -> "TestClient#getSomething".equals(properties.get("methodKey"))
                && "GET".equals(properties.get("httpMethod"))
                && "504".equals(properties.get("status"))
                && "true".equals(properties.get("retryable"))
                && "none".equals(properties.get("retryAfter"))
                && "IllegalStateException".equals(properties.get("defaultException"))),
            isNull()
        );
    }

    @Configuration
    @EnableAspectJAutoProxy
    @Import(ErrorDecoderTelemetryAspect.class)
    static class TestConfig {

        @Bean
        ErrorDecoder errorDecoder() {
            return (methodKey, response) -> new IllegalStateException("decoded");
        }
    }
}
