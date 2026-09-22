package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.sun.net.httpserver.HttpServer;
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
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.HttpClientFeignConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
 * local HTTP server that returns whatever status or delay each test asks for.
 *
 * <p>Two things are under test. First, that an external task classifies upstream failures
 * the way production traffic needs: 500 and idempotent 5xx retry with backoff, 4xx and
 * non-idempotent 5xx fail immediately. Second, that a start-message correlation only
 * retries without a tenant on the engine's 400, never on a timeout or 5xx, because the
 * first request may already have started the process.</p>
 */
@SpringBootTest(classes = FeignFailureHandlingTest.Config.class)
class FeignFailureHandlingTest {

    private static final String TENANT_BODY_MARKER = "\"tenantId\":\"civil\"";
    private static final String NO_TENANT_BODY_MARKER = "\"withoutTenantId\":true";
    private static final int READ_TIMEOUT_MS = 1000;

    private static HttpServer server;
    private static volatile int responseStatus = 200;
    private static volatile long responseDelayMs = 0;
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
            if (responseDelayMs > 0) {
                try {
                    Thread.sleep(responseDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] payload = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, payload.length);
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
        registry.add("fault.api.url", url);
        registry.add("http.client.readTimeout", () -> READ_TIMEOUT_MS);
        registry.add("http.client.connectTimeout", () -> 1000);
        registry.add("http.client.requestTimeout", () -> 1000);
        // Spring Cloud OpenFeign's per-client Request.Options override the Apache client's socket
        // timeout, and application.yaml sets the default to 30s; pin it so the delay tests time out.
        registry.add("spring.cloud.openfeign.client.config.default.readTimeout", () -> READ_TIMEOUT_MS);
        registry.add("spring.cloud.openfeign.client.config.default.connectTimeout", () -> 1000);
    }

    @BeforeEach
    void reset() {
        responseStatus = 200;
        responseDelayMs = 0;
        requestLog.clear();
    }

    @FeignClient(name = "fault-api", url = "${fault.api.url}")
    interface FaultApi {
        @GetMapping("/upstream/read")
        String read();

        @PostMapping("/upstream/write")
        String write();
    }

    /** Minimal external task handler whose business logic is one upstream Feign call. */
    static class UpstreamCallingHandler extends BaseExternalTaskHandler {
        private final Runnable upstreamCall;

        UpstreamCallingHandler(ExternalTaskCompletionService completionService, EventProperties properties, Runnable upstreamCall) {
            super(completionService, properties);
            this.upstreamCall = upstreamCall;
        }

        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            upstreamCall.run();
            return new ExternalTaskData();
        }
    }

    @Configuration
    @EnableFeignClients(clients = {CamundaRuntimeApi.class, FaultApi.class})
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
    private FaultApi faultApi;

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

        @Test
        void upstream500OnRead_isRetriedWithBackoff() {
            responseStatus = 500;

            handler(faultApi::read).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
            verify(completionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void upstream500OnWrite_isStillRetried_matchingPreHolundaBehaviour() {
            responseStatus = 500;

            handler(faultApi::write).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstream404_failsImmediatelyWithNoRetries() {
            responseStatus = 404;

            handler(faultApi::read).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), eq(1000L));
        }

        @Test
        void upstream502OnIdempotentRead_isRetried() {
            responseStatus = 502;

            handler(faultApi::read).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void upstream502OnNonIdempotentWrite_failsImmediately() {
            responseStatus = 502;

            handler(faultApi::write).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), eq(1000L));
        }

        @Test
        void upstreamReadTimeout_isRetried() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            long started = System.nanoTime();
            handler(faultApi::read).execute(externalTask, externalTaskService);
            assertThat((System.nanoTime() - started) / 1_000_000).isLessThan(responseDelayMs);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(MAX_RETRIES - 1), longThat(backoff -> backoff > 0));
        }

        @Test
        void lastRetry_decrementsToZero() {
            responseStatus = 500;
            when(externalTask.getRetries()).thenReturn(1);

            handler(faultApi::read).execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), anyString(), anyString(), eq(0), longThat(backoff -> backoff > 0));
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

        @Test
        void engineTimeout_doesNotCorrelateASecondTime() {
            responseDelayMs = READ_TIMEOUT_MS * 3L;

            long started = System.nanoTime();
            eventEmitterService.emitBusinessProcessCamundaEvent(caseData(), true);
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;

            // returned on the read timeout, not on the server's eventual 200
            assertThat(elapsedMs).isLessThan(responseDelayMs);
            assertThat(messageRequests()).hasSize(1);
            assertThat(messageRequests().get(0)).contains(TENANT_BODY_MARKER);
            verify(applicationEventPublisher, times(1)).publishEvent(any(DispatchBusinessProcessEvent.class));
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
