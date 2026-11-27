package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Retryer;
import feign.Target;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadTimeoutAwareRetryerTest {

    private static final String CCD = "core-case-data-api";
    private static final List<String> ALLOWLIST = List.of(
        CCD + ":/searchCases", "docmosis:/rs/render", "idam-api:/o/token", "case-assignment-api:/noc/check-noc-approval");
    private static final String EVENTS_URL = "http://ccd-data-store-api/cases/1234/events";
    private static final String SUBMIT_EVENT_CONFIG_KEY = "CoreCaseDataApi#submitEventForCaseWorker(String,String)";

    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp() {
        telemetryClient = mock(TelemetryClient.class);
    }

    private static RetryableException exception(Request.HttpMethod method, String url, Throwable cause) {
        return exception(CCD, method, url, cause);
    }

    /**
     * A {@code RetryableException} shaped like the one Feign raises: its request carries the
     * template of the call, with the client's target and the method metadata.
     */
    private static RetryableException exception(String clientName, Request.HttpMethod method, String url, Throwable cause) {
        RequestTemplate template = new RequestTemplate();
        if (clientName != null) {
            template.feignTarget(new Target.HardCodedTarget<>(Object.class, clientName, "http://host"));
        }
        MethodMetadata metadata = mock(MethodMetadata.class);
        when(metadata.configKey()).thenReturn(SUBMIT_EVENT_CONFIG_KEY);
        template.methodMetadata(metadata);
        Request request = Request.create(method, url, Collections.emptyMap(), null, StandardCharsets.UTF_8, template);
        return new RetryableException(-1, "error", method, cause, (Long) null, request);
    }

    private static int attempts(Retryer retryer, RetryableException e) {
        int attempts = 1;
        try {
            while (true) {
                retryer.continueOrPropagate(e);
                attempts++;
            }
        } catch (RetryableException propagated) {
            return attempts;
        }
    }

    private Retryer retryer() {
        return retryer(ALLOWLIST);
    }

    private Retryer retryer(List<String> allowlist) {
        return new ReadTimeoutAwareRetryer(
            new Retryer.Default(1, 1, 5),
            allowlist.stream().map(ReadTimeoutAwareRetryer.AllowlistEntry::parse).toList(),
            telemetryClient);
    }

    @Test
    void eventSubmitReadTimeout_isNotReplayed() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(1);
        assertThatThrownBy(() -> retryer().continueOrPropagate(e)).isSameAs(e);
    }

    @Test
    void putAndPatchReadTimeout_areNotReplayed() {
        for (Request.HttpMethod method : List.of(Request.HttpMethod.PUT, Request.HttpMethod.PATCH)) {
            RetryableException e = exception("hmc-api", method, "http://hmc/hearings/1",
                                             new SocketTimeoutException("Read timed out"));
            assertThat(attempts(retryer(), e)).isEqualTo(1);
        }
    }

    @Test
    void readTimeoutWrappedInAnotherException_isNotReplayed() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new IOException("wrapper", new SocketTimeoutException("Read timed out")));

        assertThat(attempts(retryer(), e)).isEqualTo(1);
    }

    @Test
    void readTimeoutWithNoMessage_isNotReplayed() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL, new SocketTimeoutException());

        assertThat(attempts(retryer(), e)).isEqualTo(1);
    }

    @Test
    void allowlistedReadOnlyPostReadTimeout_isReplayedUpToMaxAttempts() {
        Map<String, String> clientByPath = Map.of(
            "/searchCases?ctid=CIVIL", CCD,
            "/rs/render", "docmosis",
            "/o/token", "idam-api",
            "/noc/check-noc-approval", "case-assignment-api");
        clientByPath.forEach((path, client) -> {
            RetryableException e = exception(client, Request.HttpMethod.POST, "http://host/some" + path,
                                             new SocketTimeoutException("Read timed out"));
            assertThat(attempts(retryer(), e)).as(client + " " + path).isEqualTo(5);
        });
    }

    @Test
    void allowlistedPathFromAnotherClient_isNotReplayed() {
        RetryableException e = exception("wa", Request.HttpMethod.POST, "http://wa/searchCases",
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(1);
    }

    @Test
    void qualifiedEntry_doesNotMatchARequestWithNoFeignTarget() {
        RetryableException e = exception(null, Request.HttpMethod.POST, "http://ccd/searchCases",
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(1);
    }

    @Test
    void pathOnlyEntry_matchesAnyClient() {
        RetryableException e = exception("any-client", Request.HttpMethod.POST, "http://host/searchCases",
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(List.of("/searchCases")), e)).isEqualTo(5);
    }

    @Test
    void getReadTimeout_isReplayedUpToMaxAttempts() {
        RetryableException e = exception(Request.HttpMethod.GET, "http://ccd-data-store-api/cases/1234",
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void connectionReset_isReplayedOnPost() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL, new NoHttpResponseException("reset"));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void bareConnectTimeout_isReplayedOnPost() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new SocketTimeoutException("Connect timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    /**
     * What Apache's {@code DefaultHttpClientConnectionOperator} throws on a connect timeout.
     */
    @Test
    void apacheConnectTimeout_isReplayedOnPost() {
        ConnectTimeoutException connectTimeout = new ConnectTimeoutException(
            new SocketTimeoutException("Connect timed out"), new HttpHost("ccd-data-store-api"));
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL, connectTimeout);

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void apacheConnectTimeoutWithAnUnexpectedMessage_isStillReplayedOnPost() {
        SocketTimeoutException cause = new SocketTimeoutException("timed out");
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new ConnectTimeoutException(cause, new HttpHost("ccd-data-store-api")));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void connectionPoolTimeout_isReplayedOnPost() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new ConnectionPoolTimeoutException("Timeout waiting for connection from pool"));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void connectionRefused_isReplayedOnPost() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new HttpHostConnectException(new ConnectException("Connection refused"),
                                                                      new HttpHost("ccd-data-store-api")));

        assertThat(attempts(retryer(), e)).isEqualTo(5);
    }

    @Test
    void suppressedReplay_isRecordedWithTheFeignMethodAndClient() {
        RetryableException e = exception(Request.HttpMethod.POST, EVENTS_URL,
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(1);

        verify(telemetryClient).trackEvent(
            eq(ReadTimeoutAwareRetryer.REPLAY_SUPPRESSED_EVENT),
            eq(Map.of("method", "POST", "client", CCD, "endpoint", SUBMIT_EVENT_CONFIG_KEY)),
            isNull());
    }

    @Test
    void replayedCall_isNotRecordedAsSuppressed() {
        RetryableException e = exception(Request.HttpMethod.POST, "http://ccd/searchCases",
                                         new SocketTimeoutException("Read timed out"));

        assertThat(attempts(retryer(), e)).isEqualTo(5);

        verify(telemetryClient, never()).trackEvent(anyString(), anyMap(), any());
    }

    @Test
    void suppressedReplay_withNoTelemetryClientOrRequestTemplate_stillPropagates() {
        Retryer retryer = new ReadTimeoutAwareRetryer(ALLOWLIST, null);
        Request request = Request.create(Request.HttpMethod.POST, EVENTS_URL, Collections.emptyMap(), null,
                                         StandardCharsets.UTF_8, null);
        RetryableException e = new RetryableException(-1, "error", Request.HttpMethod.POST,
                                                      new SocketTimeoutException("Read timed out"), (Long) null, request);

        assertThatThrownBy(() -> retryer.continueOrPropagate(e)).isSameAs(e);
    }

    @Test
    void clonedRetryer_startsWithFreshAttemptCount() {
        Retryer original = retryer();
        RetryableException e = exception(Request.HttpMethod.GET, "http://host/a", new IOException("x"));
        assertThat(attempts(original, e)).isEqualTo(5);

        assertThat(attempts(original.clone(), e)).isEqualTo(5);
    }

    @Test
    void emptyAllowlist_neverReplaysWriteReadTimeouts() {
        Retryer retryer = new ReadTimeoutAwareRetryer(List.of(), telemetryClient);
        RetryableException e = exception(Request.HttpMethod.POST, "http://ccd/searchCases",
                                         new SocketTimeoutException("Read timed out"));

        assertThatCode(() -> retryer.continueOrPropagate(e)).isInstanceOf(RetryableException.class);
    }

    @Test
    void allowlistEntry_parsesClientAndPath() {
        assertThat(ReadTimeoutAwareRetryer.AllowlistEntry.parse(" core-case-data-api : /searchCases "))
            .isEqualTo(new ReadTimeoutAwareRetryer.AllowlistEntry(CCD, "/searchCases"));
        assertThat(ReadTimeoutAwareRetryer.AllowlistEntry.parse("/searchCases"))
            .isEqualTo(new ReadTimeoutAwareRetryer.AllowlistEntry(null, "/searchCases"));
    }
}
