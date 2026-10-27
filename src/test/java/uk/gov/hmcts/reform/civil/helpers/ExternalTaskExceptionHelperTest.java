package uk.gov.hmcts.reform.civil.helpers;

import feign.FeignException;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Target;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExternalTaskExceptionHelperTest {

    private static final String CCD_EVENT_SUBMIT_URL =
        "http://ccd/caseworkers/1/jurisdictions/CIVIL/case-types/CIVIL/cases/1234/events";

    private static Request post(String url) {
        return Request.create(Request.HttpMethod.POST, url, Map.of(), null, null, null);
    }

    private static Request postFromClient(String clientName, String configKey, String url) {
        RequestTemplate template = new RequestTemplate();
        if (clientName != null) {
            template.feignTarget(new Target.HardCodedTarget<>(Object.class, clientName, "http://host"));
        }
        if (configKey != null) {
            MethodMetadata metadata = mock(MethodMetadata.class);
            when(metadata.configKey()).thenReturn(configKey);
            template.methodMetadata(metadata);
        }
        return Request.create(Request.HttpMethod.POST, url, Map.of(), null, StandardCharsets.UTF_8, template);
    }

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
        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, false)).isFalse();
    }

    @Test
    void shouldReturnTrueForBadGatewayWithNonIdempotentMethodWhenGuarded() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isTrue();
    }

    @Test
    void shouldReturnFalseForServiceUnavailableWithNonIdempotentMethodWhenUnguarded() {
        Throwable exception = new FeignException.ServiceUnavailable(
            "Service unavailable",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, false)).isFalse();
    }

    @Test
    void shouldReturnTrueForServiceUnavailableWithNonIdempotentMethodWhenGuarded() {
        Throwable exception = new FeignException.ServiceUnavailable(
            "Service unavailable",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isTrue();
    }

    @Test
    void shouldReturnFalseForGatewayTimeoutWithNonIdempotentMethodWhenUnguarded() {
        Throwable exception = new FeignException.GatewayTimeout(
            "Gateway timeout",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, false)).isFalse();
    }

    @Test
    void shouldReturnTrueForGatewayTimeoutWithNonIdempotentMethodWhenGuarded() {
        Throwable exception = new FeignException.GatewayTimeout(
            "Gateway timeout",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isTrue();
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

    /**
     * A lost optimistic locking race on CCD. The write did not apply, so a retry cannot duplicate
     * anything. This must hold for POST, which is the method the submit calls use, so it proves
     * the 409 branch sits ahead of the idempotency gate rather than relying on it.
     */
    @Test
    void shouldReturnTrueForConflictOnNonIdempotentMethod() {
        Throwable exception = new FeignException.Conflict(
            "[409 Conflict] during [POST] to [/cases/1/events]",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    @Test
    void shouldReturnTrueForConflictOnIdempotentMethod() {
        Throwable exception = new FeignException.Conflict(
            "[409 Conflict] during [GET] to [/cases/1]",
            Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isTrue();
    }

    /**
     * Guards the boundary. 409 is retryable on a POST but 400 next to it must stay non retryable,
     * so the new branch cannot be read as making 4xx retryable generally.
     */
    @Test
    void shouldStillReturnFalseForOtherClientErrorsOnNonIdempotentMethod() {
        Throwable exception = new FeignException.NotFound(
            "[404 Not Found] during [POST] to [/cases/1/events]",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception)).isFalse();
    }

    @Test
    void shouldReturnTrueForConflictWrappedInACauseChain() {
        Throwable conflict = new FeignException.Conflict(
            "[409 Conflict] during [POST] to [/cases/1/events]",
            Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(new RuntimeException("wrapped", conflict))).isTrue();
    }

    @Test
    void shouldReturnTrueForGuardedBadGatewayWrappedInACauseChain() {
        Throwable badGateway = new FeignException.BadGateway(
            "Bad gateway",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(new RuntimeException("wrapped", badGateway), true)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isRetryable(new RuntimeException("wrapped", badGateway), false)).isFalse();
    }

    @Test
    void shouldReturnTrueForGuardedBadGatewayOnEventSubmitWithQueryString() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            post(CCD_EVENT_SUBMIT_URL + "?ignore-warning=true"),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isTrue();
    }

    @Test
    void shouldReturnFalseForGuardedBadGatewayOnPostThatIsNotAnEventSubmit() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            post("http://documents/cases/documents"),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isFalse();
    }

    @Test
    void shouldReturnFalseForGuardedServiceUnavailableOnEventsLookalikePath() {
        Throwable exception = new FeignException.ServiceUnavailable(
            "Service unavailable",
            post("http://ccd/cases/1/events/archive"),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isFalse();
    }

    @Test
    void shouldReturnFalseForGuardedBadGatewayWhenRequestIsMissing() {
        FeignException exception = mock(FeignException.class);
        when(exception.status()).thenReturn(502);
        when(exception.request()).thenReturn(null);

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isFalse();
    }

    @Test
    void shouldIdentifyGatewayErrors() {
        assertThat(ExternalTaskExceptionHelper.isGatewayError(502)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isGatewayError(503)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isGatewayError(504)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isGatewayError(500)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isGatewayError(404)).isFalse();
    }

    @Test
    void shouldIdentifyCcdEventSubmitByPathSuffix() {
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(post(CCD_EVENT_SUBMIT_URL))).isTrue();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(post(CCD_EVENT_SUBMIT_URL + "?a=b"))).isTrue();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(post("http://ccd/searchCases"))).isFalse();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(post("http://ccd/x?next=/events"))).isFalse();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(null)).isFalse();
    }

    @Test
    void shouldIdentifyEventSubmitOnlyFromTheCcdClientWhenTheClientIsKnown() {
        Request fromCcd = postFromClient("core-case-data-api", null, CCD_EVENT_SUBMIT_URL);
        Request fromOtherClient = postFromClient("some-other-api", null, CCD_EVENT_SUBMIT_URL);
        Request noTarget = postFromClient(null, null, CCD_EVENT_SUBMIT_URL);

        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(fromCcd)).isTrue();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(fromOtherClient)).isFalse();
        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(noTarget)).isTrue();
    }

    @Test
    void shouldNotRetryGuardedBadGatewayOnEventsPathFromAnotherClient() {
        Throwable exception = new FeignException.BadGateway(
            "Bad gateway",
            postFromClient("some-other-api", null, "http://other/cases/1/events"),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isFalse();
    }

    @Test
    void shouldDescribeEndpointByFeignMethodKey() {
        Request withKey = postFromClient("core-case-data-api", "CoreCaseDataApi#submitEventForCaseWorker(..)",
                                         CCD_EVENT_SUBMIT_URL);

        assertThat(ExternalTaskExceptionHelper.describeEndpoint(withKey))
            .isEqualTo("CoreCaseDataApi#submitEventForCaseWorker(..)");
        assertThat(ExternalTaskExceptionHelper.describeEndpoint(post(CCD_EVENT_SUBMIT_URL))).isEqualTo("unknown");
        assertThat(ExternalTaskExceptionHelper.describeEndpoint(postFromClient("core-case-data-api", null, CCD_EVENT_SUBMIT_URL)))
            .isEqualTo("unknown");
        assertThat(ExternalTaskExceptionHelper.describeEndpoint(null)).isEqualTo("unknown");
    }

    @Test
    void shouldNotIdentifyNonPostRequestOnEventsPathAsEventSubmit() {
        Request get = Request.create(Request.HttpMethod.GET, CCD_EVENT_SUBMIT_URL, Map.of(), null, null, null);

        assertThat(ExternalTaskExceptionHelper.isCcdEventSubmit(get)).isFalse();
    }

    @Test
    void shouldNotFindGuardedGatewayFailureForGetOnEventsPath() {
        FeignException badGateway = new FeignException.BadGateway(
            "Bad gateway",
            Request.create(Request.HttpMethod.GET, CCD_EVENT_SUBMIT_URL, Map.of(), null, null, null),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.findGuardedGatewayFailure(badGateway)).isNull();
    }

    @Test
    void shouldFindGuardedGatewayFailureInACauseChain() {
        FeignException badGateway = new FeignException.BadGateway(
            "Bad gateway",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );
        FeignException notFound = new FeignException.NotFound(
            "Not found",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.findGuardedGatewayFailure(new RuntimeException("wrapped", badGateway)))
            .isSameAs(badGateway);
        assertThat(ExternalTaskExceptionHelper.findGuardedGatewayFailure(notFound)).isNull();
        assertThat(ExternalTaskExceptionHelper.findGuardedGatewayFailure(new RuntimeException("plain"))).isNull();
    }

    @Test
    void shouldStillReturnFalseForOtherClientErrorsOnNonIdempotentMethodEvenWhenGuarded() {
        Throwable exception = new FeignException.NotFound(
            "[404 Not Found] during [POST] to [/cases/1/events]",
            post(CCD_EVENT_SUBMIT_URL),
            null,
            null
        );

        assertThat(ExternalTaskExceptionHelper.isRetryable(exception, true)).isFalse();
    }
}
