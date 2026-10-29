package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.sun.net.httpserver.HttpServer;
import feign.FeignException;
import feign.RetryableException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.civil.config.HttpClientFeignConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.ExternalTaskExceptionHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Drives the failure paths that the happy-path regression never reaches, through the real
 * Feign stack (Spring Cloud OpenFeign, the production {@link HttpClientFeignConfiguration}
 * with its Apache client, read timeout and default {@code ErrorDecoder} bean) against a
 * local HTTP server that returns whatever status or delay each test asks for. Only the
 * production {@link CamundaRuntimeApi} and {@link CoreCaseDataApi} clients are registered: a
 * test-only {@code @FeignClient} would be picked up by the application's
 * {@code @EnableFeignClients} scan in the integration and contract test contexts, where its URL
 * property does not exist.
 *
 * <p>Two things are under test. First, that an external task classifies upstream failures
 * the way production traffic needs: 500 and idempotent 5xx retry with backoff, 4xx and
 * non-idempotent 5xx fail immediately. Second, that a start-message correlation only
 * retries without a tenant on the engine's 400, never on a timeout or 5xx, because the
 * first request may already have started the process.</p>
 */
@SpringBootTest(classes = FeignFailureHandlingTest.Config.class)
@ActiveProfiles(FeignFailureHandlingTest.PROFILE)
class FeignFailureHandlingTest {

    /**
     * {@code Application} declares {@code @ComponentScan("uk.gov.hmcts.reform")} without Spring
     * Boot's test-type exclude filter, so every full-application context (integration and
     * contract tests share this classpath) would scan the nested {@link Config} in and its
     * {@code AuthTokenGenerator} bean would clash with the production one. Gating the config on a
     * profile that only this test activates keeps it out of those contexts.
     */
    static final String PROFILE = "feign-failure-handling-test";

    private static final String TENANT_BODY_MARKER = "\"tenantId\":\"civil\"";
    private static final String NO_TENANT_BODY_MARKER = "\"withoutTenantId\":true";
    private static final int READ_TIMEOUT_MS = 1000;
    /**
     * {@link feign.Retryer.Default} attempts a call 5 times before giving up.
     */
    private static final int FEIGN_MAX_ATTEMPTS = 5;

    private static HttpServer server;
    private static volatile int responseStatus = 200;
    private static volatile long responseDelayMs = 0;
    /**
     * When positive, delays only the first request; later ones use {@code responseDelayMs}.
     */
    private static volatile long firstRequestDelayMs = 0;
    /**
     * When positive, every request after the first answers with this status and CCD's 404 body.
     */
    private static volatile int laterRequestStatus = 0;
    private static final AtomicInteger requestCount = new AtomicInteger();
    private static final List<String> requestLog = new CopyOnWriteArrayList<>();

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // a delayed handler from a timeout test must not block the next test's request
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", exchange -> {
            String body;
            try (InputStream in = exchange.getRequestBody()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            requestLog.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " " + body);
            boolean replay = requestCount.incrementAndGet() > 1;
            long delay = !replay && firstRequestDelayMs > 0 ? firstRequestDelayMs : responseDelayMs;
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            boolean staleReplay = replay && laterRequestStatus > 0;
            byte[] payload = (staleReplay ? "{\"message\":\"No field found\"}" : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(staleReplay ? laterRequestStatus : responseStatus, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
        });
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        Supplier<Object> url = () -> "http://127.0.0.1:" + server.getAddress().getPort();
        registry.add("feign.client.config.processInstance.url", url);
        registry.add("core_case_data.api.url", url);
        registry.add("http.client.readTimeout", () -> READ_TIMEOUT_MS);
        registry.add("http.client.connectTimeout", () -> 1000);
        registry.add("http.client.requestTimeout", () -> 1000);
        // Spring Cloud OpenFeign's per-client Request.Options override the Apache client's socket
        // timeout, and application.yaml sets the default to 30s; pin it so the delay tests time out.
        registry.add("spring.cloud.openfeign.client.config.default.readTimeout", () -> READ_TIMEOUT_MS);
        registry.add("spring.cloud.openfeign.client.config.default.connectTimeout", () -> 1000);
        // application.yaml gives the CCD client its own 25s read timeout, as in production
        registry.add("spring.cloud.openfeign.client.config.core-case-data-api.readTimeout", () -> READ_TIMEOUT_MS);
    }

    @BeforeEach
    void reset() {
        responseStatus = 200;
        responseDelayMs = 0;
        firstRequestDelayMs = 0;
        laterRequestStatus = 0;
        requestCount.set(0);
        requestLog.clear();
    }

    /** Minimal external task handler whose business logic is one upstream Feign call. */
    static class UpstreamCallingHandler extends BaseExternalTaskHandler {
        private final Runnable upstreamCall;
        private final boolean retryGuarded;

        UpstreamCallingHandler(ExternalTaskCompletionService completionService, EventProperties properties, Runnable upstreamCall) {
            this(completionService, properties, upstreamCall, false);
        }

        UpstreamCallingHandler(ExternalTaskCompletionService completionService, EventProperties properties, Runnable upstreamCall, boolean retryGuarded) {
            super(completionService, properties);
            this.upstreamCall = upstreamCall;
            this.retryGuarded = retryGuarded;
        }

        @Override
        protected boolean isRetryGuarded() {
            return retryGuarded;
        }

        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            upstreamCall.run();
            return new ExternalTaskData();
        }
    }

    @Configuration
    @Profile(PROFILE)
    @EnableFeignClients(clients = {CamundaRuntimeApi.class, CoreCaseDataApi.class})
    @ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, JacksonAutoConfiguration.class})
    @Import({HttpClientFeignConfiguration.class, CamundaRuntimeClient.class, EventEmitterService.class})
    static class Config {

        @Bean
        AuthTokenGenerator authTokenGenerator() {
            AuthTokenGenerator generator = mock(AuthTokenGenerator.class);
            when(generator.generate()).thenReturn("s2s-token");
            return generator;
        }

        @Bean
        ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }
    }

    @MockBean
    private TelemetryClient telemetryClient;

    @Autowired
    private CamundaRuntimeClient camundaClient;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private EventEmitterService eventEmitterService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class ExternalTaskRetryClassification {

        private static final int MAX_RETRIES = 3;

        private ExternalTaskService externalTaskService;
        private ExternalTask externalTask;
        private ExternalTaskCompletionService completionService;
        private EventProperties eventProperties;

        @BeforeEach
        void setUp() {
            externalTaskService = mock(ExternalTaskService.class);
            completionService = mock(ExternalTaskCompletionService.class);
            externalTask = mock(ExternalTask.class);
            when(externalTask.getTopicName()).thenReturn("UPSTREAM_CALL");
            when(externalTask.getProcessInstanceId()).thenReturn("proc-1");
            when(externalTask.getRetries()).thenReturn(MAX_RETRIES);
            eventProperties = new EventProperties();
            eventProperties.setRetryCount(MAX_RETRIES);
            eventProperties.setBackoffDelay(500);
            eventProperties.setLockDuration(2000L);
        }

        private UpstreamCallingHandler handler(Runnable call) {
            return new UpstreamCallingHandler(completionService, eventProperties, call);
        }

        private UpstreamCallingHandler guardedHandler(Runnable call) {
            return new UpstreamCallingHandler(completionService, eventProperties, call, true);
        }

        @Test
        void upstream500OnRead_isRetriedWithBackoff() {
            responseStatus = 500;

            handler(() -> camundaClient.getProcessVariables("proc-1")).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
            verify(completionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void upstream500OnWrite_isStillRetried_matchingPreHolundaBehaviour() {
            responseStatus = 500;

            handler(() -> camundaClient.setProcessVariables("proc-1", Map.of("flag", true))).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstream404_failsImmediatelyWithNoRetries() {
            responseStatus = 404;

            handler(() -> camundaClient.getProcessVariables("proc-1")).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), eq(1000L));
        }

        @Test
        void upstream502OnIdempotentRead_isRetried() {
            responseStatus = 502;

            handler(() -> camundaClient.getProcessVariables("proc-1")).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstream502OnNonIdempotentWrite_failsImmediatelyWhenUnguarded() {
            responseStatus = 502;

            handler(() -> camundaClient.setProcessVariables("proc-1", Map.of("flag", true))).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), eq(1000L));
        }

        @Test
        void upstream502OnNonIdempotentWrite_isRetriedWhenGuarded() {
            responseStatus = 502;

            guardedHandler(FeignFailureHandlingTest.this::submitCcdEvent).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstreamReadTimeout_isRetried() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            long started = System.nanoTime();
            handler(() -> camundaClient.getProcessVariables("proc-1")).execute(externalTask, externalTaskService);
            // every attempt aborts on the read timeout rather than waiting out the server, so the
            // total stays well under the cost of sitting through the delay on each one
            assertThat((System.nanoTime() - started) / 1_000_000)
                .isLessThan(responseDelayMs * FEIGN_MAX_ATTEMPTS);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstreamReadTimeoutOnWrite_isSentOnceAndLeftToTheTaskRetry() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            handler(() -> camundaClient.setProcessVariables("proc-1", Map.of("flag", true))).execute(externalTask, externalTaskService);

            assertThat(requestLog)
                .as("a write that timed out on read must not be replayed with the same payload")
                .hasSize(1);
            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void ccdEventSubmitReadTimeout_isSentOnceAndLeftToTheTaskRetry() {
            firstRequestDelayMs = READ_TIMEOUT_MS * 3L;
            // what CCD answers a replay with once the event token is stale
            laterRequestStatus = 404;

            handler(() -> submitCcdEvent()).execute(externalTask, externalTaskService);

            assertThat(requestLog)
                .as("the event submit must not be replayed with the same token")
                .hasSize(1);
            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
            verify(completionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void lastRetry_decrementsToZero() {
            responseStatus = 500;
            when(externalTask.getRetries()).thenReturn(1);

            handler(() -> camundaClient.getProcessVariables("proc-1")).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), longThat(backoff -> backoff > 0));
        }
    }

    private void submitCcdEvent() {
        coreCaseDataApi.submitEventForCaseWorker(
            "Bearer user-token", "s2s-token", "user-1", "CIVIL", "CIVIL", "1792636528942239", true,
            CaseDataContent.builder()
                .event(Event.builder().id("CREATE_CLAIM").build())
                .eventToken("token-1")
                .data(Map.of())
                .build());
    }

    private List<String> requestsTo(String pathSuffix) {
        return requestLog.stream().filter(r -> r.split(" ")[1].endsWith(pathSuffix)).toList();
    }

    /**
     * The CCD event submit from the ticket, through the production {@link CoreCaseDataApi} client
     * and the real Feign stack. A timed-out submit that is replayed with the same event token is
     * rejected by CCD with a 404 "No field found", which the task handler does not retry, so the
     * case stalls. The submit must be sent once and surface as retryable instead.
     */
    @Nested
    class CcdEventSubmit {

        @Test
        void readTimeout_isSentOnceAndSurfacesAsRetryable() {
            firstRequestDelayMs = READ_TIMEOUT_MS * 3L;
            laterRequestStatus = 404;

            assertThatThrownBy(FeignFailureHandlingTest.this::submitCcdEvent)
                .isInstanceOf(RetryableException.class)
                .satisfies(e -> assertThat(ExternalTaskExceptionHelper.isRetryable(e)).isTrue());

            assertThat(requestsTo("/events")).hasSize(1);
            assertThat(requestLog).hasSize(1);
            // the real request template carries the client name and Feign method, so the suppressed
            // replay can be traced to the endpoint in production
            verify(telemetryClient).trackEvent(
                eq("feign.replay.suppressed"),
                argThat(properties -> "POST".equals(properties.get("method"))
                    && "core-case-data-api".equals(properties.get("client"))
                    && properties.get("endpoint").startsWith("CoreCaseDataApi#submitEventForCaseWorker(")),
                isNull());
        }

        @Test
        void staleTokenReplay404_isNotRetryable() {
            responseStatus = 404;

            assertThatThrownBy(FeignFailureHandlingTest.this::submitCcdEvent)
                .isInstanceOf(FeignException.NotFound.class)
                .satisfies(e -> assertThat(ExternalTaskExceptionHelper.isRetryable(e)).isFalse());

            assertThat(requestsTo("/events")).hasSize(1);
        }

        @Test
        void searchCasesReadTimeout_keepsTheReplay() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            assertThatThrownBy(() -> coreCaseDataApi.searchCases("Bearer user-token", "s2s-token", "CIVIL", "{}"))
                .isInstanceOf(RetryableException.class);

            assertThat(requestsTo("/searchCases"))
                .as("a read-only POST on the allowlist is still replayed")
                .hasSize(FEIGN_MAX_ATTEMPTS);
        }

        @Test
        void startEventTokenReadTimeout_keepsTheReplay() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            assertThatThrownBy(() -> coreCaseDataApi.startEventForCaseWorker(
                "Bearer user-token", "s2s-token", "user-1", "CIVIL", "CIVIL", "1792636528942239", "CREATE_CLAIM"))
                .isInstanceOf(RetryableException.class);

            assertThat(requestsTo("/token")).hasSize(FEIGN_MAX_ATTEMPTS);
        }
    }

    @Nested
    class StartMessageCorrelationFallback {

        private CaseData caseData() {
            return CaseData.builder()
                .ccdCaseReference(1234567890123456L)
                .businessProcess(new BusinessProcess().setCamundaEvent("TEST_EVENT"))
                .build();
        }

        private List<String> messageRequests() {
            return requestLog.stream().filter(r -> r.contains("/message")).toList();
        }

        @Test
        void engine400_correlatesAgainWithoutTenant() {
            responseStatus = 400;

            eventEmitterService.emitBusinessProcessCamundaEvent(caseData(), false);

            List<String> requests = messageRequests();
            assertThat(requests).hasSize(2);
            assertThat(requests.get(0)).contains(TENANT_BODY_MARKER).doesNotContain(NO_TENANT_BODY_MARKER);
            assertThat(requests.get(1)).contains(NO_TENANT_BODY_MARKER).doesNotContain(TENANT_BODY_MARKER);
        }

        /**
         * A read timeout must not trigger the untenanted fallback that a 400 does, and must not
         * dispatch the business process more than once.
         *
         * <p>The correlation is a POST, so the Feign retryer must not replay it after a read
         * timeout: the engine may already have started the process, and a replay would start it
         * again.</p>
         */
        @Test
        void engineTimeout_doesNotFallBackToAnUntenantedCorrelation() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            eventEmitterService.emitBusinessProcessCamundaEvent(caseData(), true);

            assertThat(messageRequests())
                .as("a write that timed out on read is not replayed")
                .hasSize(1);
            assertThat(messageRequests())
                .as("the attempt must carry the tenant; the untenanted fallback is for a 400 only")
                .allSatisfy(request -> assertThat(request)
                    .contains(TENANT_BODY_MARKER)
                    .doesNotContain(NO_TENANT_BODY_MARKER));
            verify(applicationEventPublisher, times(1))
                .publishEvent(any(DispatchBusinessProcessEvent.class));
        }

        @Test
        void engine502_doesNotCorrelateASecondTime() {
            responseStatus = 502;

            eventEmitterService.emitBusinessProcessCamundaEvent(caseData(), false);

            assertThat(messageRequests()).hasSize(1);
        }

        @Test
        void engine200_correlatesOnceWithTenantAndTypedVariables() throws Exception {
            eventEmitterService.emitBusinessProcessCamundaEvent(caseData(), false);

            List<String> requests = messageRequests();
            assertThat(requests).hasSize(1);
            String body = requests.get(0).substring(requests.get(0).indexOf('{'));
            var json = objectMapper.readTree(body);
            assertThat(json.get("messageName").asText()).isEqualTo("TEST_EVENT");
            assertThat(json.get("tenantId").asText()).isEqualTo("civil");
            assertThat(json.get("processVariables").get("caseId").get("type").asText()).isEqualTo("Long");
            assertThat(json.get("processVariables").get("caseId").get("value").asLong()).isEqualTo(1234567890123456L);
        }
    }
}
