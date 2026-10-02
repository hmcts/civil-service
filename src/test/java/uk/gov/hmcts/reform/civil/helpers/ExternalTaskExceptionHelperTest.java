package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExternalTaskExceptionHelperTest {

    @Test
    void shouldReturnTrueForNullThrowable() {
        assertThat(ExternalTaskExceptionHelper.isRetryable(null)).isTrue();
    }

    @Test
    void shouldReturnFalseForBadRequest() {
        Throwable exception = new FeignException.BadRequest(
            "Bad request",
            Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnTrueForBadGatewayWithIdempotentMethod() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnFalseForBadGatewayWithNonIdempotentMethod() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenFeignStatusIsUnavailableForIdempotentMethod() {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(-1);
        when(exception.request()).thenReturn(Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null));

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnTrueForRetryableException() {
        RetryableException exception = mock(RetryableException.class);

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnTrueForWrappedRetryableException() {
        RuntimeException exception = new RuntimeException(mock(RetryableException.class));

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnFalseForRetryableStatusWhenRequestIsUnavailable() {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(503);
        when(exception.request()).thenReturn(null);

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenFeignStatusIsUnavailableForNonIdempotentMethod() {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(-1);
        when(exception.request()).thenReturn(Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null));

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnTrueForNonFeignFailureRegardlessOfMessage() {
        Throwable exception = new RuntimeException(
            "Error during remote invocation of DocmosisApiClient#createDocument(DocmosisRequest): Bad Request"
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnTrueForInternalServerErrorOnNonIdempotentMethod() {
        Throwable exception = new FeignException.InternalServerError(
            "Internal server error",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnFalseForFeignExceptionWrappedInAnotherException() {
        Throwable exception = new IllegalStateException(
            "wrapper",
            new FeignException.BadRequest(
                "Bad request",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
                null,
                null
            )
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnNullStackTraceForNullThrowable() {
        Logger log = mock(Logger.class);

        assertThat(ExternalTaskExceptionHelper.getStackTrace(null)).isNull();
        verifyNoInteractions(log);
    }

    @Test
    void shouldReturnStackTraceWhenLoggerIsNull() {
        assertThat(ExternalTaskExceptionHelper.getStackTrace(new RuntimeException("boom")))
            .contains("ExternalTaskExceptionHelperTest");
    }
}
