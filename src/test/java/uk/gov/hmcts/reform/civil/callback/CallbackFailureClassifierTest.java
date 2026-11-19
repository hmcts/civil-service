package uk.gov.hmcts.reform.civil.callback;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackFailureClassifier.NOT_RETRYABLE;
import static uk.gov.hmcts.reform.civil.callback.CallbackFailureClassifier.RETRYABLE;

class CallbackFailureClassifierTest {

    private static Request request(Request.HttpMethod method) {
        return Request.create(method, "https://docmosis/rs/render", Map.of(), null, StandardCharsets.UTF_8, null);
    }

    @Test
    void upstreamBadGatewayOnAPostIsTransient() {
        // A failed callback means CCD saved nothing, so even a POST to another service is safe to retry
        FeignException badGateway = new FeignException.BadGateway("Bad Gateway", request(Request.HttpMethod.POST), null, null);

        assertThat(CallbackFailureClassifier.isTransient(badGateway)).isTrue();
    }

    @Test
    void upstreamConflictAndTooManyRequestsAreTransient() {
        assertThat(CallbackFailureClassifier.isTransient(
            new FeignException.Conflict("Conflict", request(Request.HttpMethod.POST), null, null))).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(
            new FeignException.TooManyRequests("Too many", request(Request.HttpMethod.GET), null, null))).isTrue();
    }

    @Test
    void upstreamBadRequestIsNotTransient() {
        FeignException badRequest = new FeignException.BadRequest("Bad Request", request(Request.HttpMethod.POST), null, null);

        assertThat(CallbackFailureClassifier.isTransient(badRequest)).isFalse();
    }

    @Test
    void docmosisInternalNullPointerOnRenderIsTransient() {
        // Docmosis's own glitch: 400 with its internal NPE, seen in bursts across unrelated templates
        FeignException docmosisGlitch = new FeignException.BadRequest(
            "[400 Bad Request] during [POST] to [https://docmosis/rs/render]", request(Request.HttpMethod.POST),
            "{\"succeeded\":false,\"shortMsg\":\"java.lang.NullPointerException\"}".getBytes(StandardCharsets.UTF_8), null);

        assertThat(CallbackFailureClassifier.isTransient(docmosisGlitch)).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(new RuntimeException("render failed", docmosisGlitch))).isTrue();
        assertThat(CallbackFailureClassifier.describe(docmosisGlitch)).startsWith(RETRYABLE);
    }

    @Test
    void otherDocmosisBadRequestsAreNotTransient() {
        FeignException badTemplate = new FeignException.BadRequest(
            "[400 Bad Request]", request(Request.HttpMethod.POST),
            "{\"succeeded\":false,\"shortMsg\":\"Template not found: CV-XXX.docx\"}".getBytes(StandardCharsets.UTF_8), null);

        assertThat(CallbackFailureClassifier.isTransient(badTemplate)).isFalse();
    }

    @Test
    void nullPointerBodyFromAnotherServiceIsNotTransient() {
        Request otherService = Request.create(Request.HttpMethod.POST, "http://ccd-data-store-api/cases/1/events",
                                              Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException badRequest = new FeignException.BadRequest(
            "[400 Bad Request]", otherService,
            "{\"succeeded\":false,\"shortMsg\":\"java.lang.NullPointerException\"}".getBytes(StandardCharsets.UTF_8), null);

        assertThat(CallbackFailureClassifier.isTransient(badRequest)).isFalse();
    }

    @Test
    void refDataAuthBlipsOnOrganisationLookupsAreTransient() {
        Request orgLookup = Request.create(Request.HttpMethod.GET,
            "http://rd-professional-api-prod.service.core-compute-prod.internal/refdata/internal/v1/organisations?id=4GY5ONN",
            Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException forbidden = new FeignException.Forbidden("[403 Forbidden]", orgLookup,
            "{\"errorMessage\":null,\"errorDescription\":null}".getBytes(StandardCharsets.UTF_8), null);
        FeignException unauthorised = new FeignException.Unauthorized("[401 Unauthorized]", orgLookup,
            "{\"errorMessage\":\"Error while invoking UP\"}".getBytes(StandardCharsets.UTF_8), null);

        assertThat(CallbackFailureClassifier.isTransient(forbidden)).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(new RuntimeException("rpa feed", unauthorised))).isTrue();
    }

    @Test
    void refDataNotFoundAndOtherServicesAuthFailuresAreNotTransient() {
        Request orgLookup = Request.create(Request.HttpMethod.GET,
            "http://rd-professional-api-prod.service.core-compute-prod.internal/refdata/internal/v1/organisations?id=XXXXXXX",
            Map.of(), null, StandardCharsets.UTF_8, null);
        Request cdam = Request.create(Request.HttpMethod.POST,
            "http://ccd-case-document-am-api-prod.service.core-compute-prod.internal/cases/documents",
            Map.of(), null, StandardCharsets.UTF_8, null);

        assertThat(CallbackFailureClassifier.isTransient(
            new FeignException.NotFound("[404 Not Found]", orgLookup, null, null))).isFalse();
        assertThat(CallbackFailureClassifier.isTransient(
            new FeignException.Forbidden("[403 Forbidden]", cdam, null, null))).isFalse();
    }

    @Test
    void notRetryableCallbackErrorIsExtractedFromCcdResponse() {
        String body = "{\"status\":422,\"callbackErrors\":[\"NOT RETRYABLE: java.lang.NullPointerException: "
            + "Cannot invoke \\\"Party.getPartyName()\\\"\"],\"callbackWarnings\":[]}";

        assertThat(CallbackFailureClassifier.notRetryableCallbackError(body))
            .isEqualTo("NOT RETRYABLE: java.lang.NullPointerException: Cannot invoke \"Party.getPartyName()\"");
        assertThat(CallbackFailureClassifier.notRetryableCallbackError(
            "{\"callbackErrors\":[\"RETRYABLE: feign.FeignException$BadGateway: x\"]}")).isNull();
        assertThat(CallbackFailureClassifier.notRetryableCallbackError(
            "{\"callbackErrors\":[\"Event X is already processed\"]}")).isNull();
        assertThat(CallbackFailureClassifier.notRetryableCallbackError(null)).isNull();
    }

    @Test
    void codeFaultsAreNotTransient() {
        assertThat(CallbackFailureClassifier.isTransient(new NullPointerException("repayment plan is null"))).isFalse();
        assertThat(CallbackFailureClassifier.isTransient(new IllegalArgumentException("Unexpected hearing type"))).isFalse();
    }

    @Test
    void transientCauseAnywhereInTheChainCounts() {
        RuntimeException wrapped = new RuntimeException("stitching failed", new SocketTimeoutException("Read timed out"));
        RetryableException retryable = new RetryableException(
            -1, "connection reset", Request.HttpMethod.POST, (Long) null, request(Request.HttpMethod.POST));

        assertThat(CallbackFailureClassifier.isTransient(wrapped)).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(retryable)).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(
            new UpstreamUnavailableException("CCD", "1234", null, null))).isTrue();
        assertThat(CallbackFailureClassifier.isTransient(
            new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))).isTrue();
    }

    @Test
    void describeLabelsTheExceptionClassAndMessage() {
        assertThat(CallbackFailureClassifier.describe(new NullPointerException("repayment plan is null")))
            .isEqualTo(NOT_RETRYABLE + "java.lang.NullPointerException: repayment plan is null");
        assertThat(CallbackFailureClassifier.describe(
            new FeignException.BadGateway("[502 Bad Gateway] during [POST]", request(Request.HttpMethod.POST), null, null)))
            .startsWith(RETRYABLE + "feign.FeignException$BadGateway: [502 Bad Gateway]");
    }

    @Test
    void describeCopesWithANullMessageAndCapsTheLength() {
        assertThat(CallbackFailureClassifier.describe(new NullPointerException()))
            .isEqualTo(NOT_RETRYABLE + "java.lang.NullPointerException: ");
        assertThat(CallbackFailureClassifier.describe(new IllegalStateException("x".repeat(5000))))
            .hasSize(CallbackFailureClassifier.MAX_ERROR_LENGTH);
    }

    @Test
    void retryableCallbackErrorIsRecognisedInCcdsUnprocessableEntity() {
        assertThat(CallbackFailureClassifier.isRetryableCallbackError(unprocessable(
            "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.ApiException\",\"status\":422,"
                + "\"callbackErrors\":[\"RETRYABLE: feign.FeignException$BadGateway: [502 Bad Gateway]\"]}"))).isTrue();
    }

    @Test
    void notRetryableOrUnlabelledCallbackErrorsAreNotRetryable() {
        assertThat(CallbackFailureClassifier.isRetryableCallbackError(unprocessable(
            "{\"callbackErrors\":[\"NOT RETRYABLE: java.lang.NullPointerException: x\"]}"))).isFalse();
        assertThat(CallbackFailureClassifier.isRetryableCallbackError(unprocessable(
            "{\"callbackErrors\":[\"Event GENERALAPPLICATION-ASSIGN_GA_ROLES is already processed\"]}"))).isFalse();
        assertThat(CallbackFailureClassifier.isRetryableCallbackError(
            new FeignException.BadGateway("Bad Gateway", request(Request.HttpMethod.POST),
                                          "{\"callbackErrors\":[\"RETRYABLE: x\"]}".getBytes(StandardCharsets.UTF_8), null)))
            .isFalse();
        assertThat(CallbackFailureClassifier.isRetryableCallbackError(null)).isFalse();
    }

    private static FeignException unprocessable(String body) {
        return new FeignException.UnprocessableEntity(
            "[422 Unprocessable Entity]", request(Request.HttpMethod.POST), body.getBytes(StandardCharsets.UTF_8), null);
    }
}
