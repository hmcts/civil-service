package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.Request;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ExternalTaskExceptionHelperTest {

    @Test
    void shouldReturnFalseForNullThrowable() {
        assertThat(ExternalTaskExceptionHelper.isNotRetryable(null)).isFalse();
    }

    @Test
    void shouldReturnTrueForRemoteProcessEngineBadRequest() {
        Throwable exception = new RemoteProcessEngineException(
            "REST-CLIENT-001 Error during remote Camunda engine invocation: Bad Request",
            new FeignException.BadRequest(
                "Bad request",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
                null,
                null
            )
        );

        assertThat(ExternalTaskExceptionHelper.isNotRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnFalseForRemoteProcessEngineBadGateway() {
        Throwable exception = new RemoteProcessEngineException(
            "REST-CLIENT-001 Error during remote Camunda engine invocation: Bad Gateway",
            new FeignException.BadGateway(
                "Bad gateway",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
                null,
                null
            )
        );

        assertThat(ExternalTaskExceptionHelper.isNotRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnTrueForRemoteProcessEngineBadRequestMessageWithoutCause() {
        Throwable exception = new RemoteProcessEngineException(
            "REST-CLIENT-001 Error during remote Camunda engine invocation of DocmosisApiClient#createDocument(DocmosisRequest): Bad Request",
            null
        );

        assertThat(ExternalTaskExceptionHelper.isNotRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnFalseForRetryableStatuses() {
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(408)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(429)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(502)).isFalse();
    }

    @Test
    void shouldReturnTrueForOtherClientErrorStatuses() {
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(400)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(401)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(403)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(404)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isNotRetryableClientStatus(422)).isTrue();
    }

    @Test
    void shouldReturnNullStackTraceForNullThrowable() {
        Logger log = mock(Logger.class);

        assertThat(ExternalTaskExceptionHelper.getStackTrace(null, log)).isNull();
        verifyNoInteractions(log);
    }

    @Test
    void shouldReturnStackTraceWhenLoggerIsNull() {
        assertThat(ExternalTaskExceptionHelper.getStackTrace(new RuntimeException("boom"), null))
            .contains("ExternalTaskExceptionHelperTest");
    }
}
