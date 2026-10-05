package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

import java.util.Set;

@Configuration
@EnableRetry
public class ExternalTaskListenerConfiguration {

    private final String baseUrl;
    private final AuthTokenGenerator authTokenGenerator;
    private final EventProperties eventProperties;

    @Autowired
    public ExternalTaskListenerConfiguration(@Value("${feign.client.config.processInstance.url}") String baseUrl,
                                             AuthTokenGenerator authTokenGenerator,
                                             EventProperties eventProperties) {
        this.baseUrl = baseUrl;
        this.authTokenGenerator = authTokenGenerator;
        this.eventProperties = eventProperties;
    }

    /**
     * Backoff applied by the external task client between {@code fetchAndLock} attempts.
     *
     * <p>Without a real backoff a transient upstream outage - the gateway returning
     * 502/503/504 error pages that the client cannot parse into an {@code EngineRestExceptionDto} -
     * becomes a tight, zero-delay retry loop that hammers Camunda and floods the logs with
     * {@code EngineClientException} (EXC-CS-020).
     *
     * <p>{@link CamundaErrorAwareBackoffStrategy} only backs off when a {@code fetchAndLock} call
     * actually errors; a successful or empty poll stays at {@code 0ms} (the 29.5s long-poll already
     * paces the loop). Because the backoff never touches healthy pickup latency the cap can be set
     * high enough - {@code EVENT_CLIENT_BACKOFF_MAX}, default 60s - to genuinely throttle a retry
     * storm. Pairs with {@link NonJsonCamundaErrorResponseInterceptor}, which turns the otherwise
     * status-less parse failure into a typed 5xx error this strategy can see.
     */
    @Bean
    public BackoffStrategy externalTaskBackoffStrategy() {
        return newBackoffStrategy();
    }

    /**
     * A fresh strategy per client. {@link CamundaErrorAwareBackoffStrategy} holds the consecutive
     * error count in an {@code AtomicInteger}, so sharing one instance between the two clients
     * would let the scheduler client's errors back off the case driven client and vice versa,
     * which is the cross contamination the split exists to prevent. Each client therefore gets its
     * own counter rather than the singleton bean above.
     */
    BackoffStrategy newBackoffStrategy() {
        return new CamundaErrorAwareBackoffStrategy(
            eventProperties.getClientBackoffInitial(),
            eventProperties.getClientBackoffFactor(),
            eventProperties.getClientBackoffMax());
    }

    /**
     * HttpClient 5 pool tuning for Camunda {@code fetchAndLock} (EXC-CS-037).
     *
     * <p>Default {@code validateAfterInactivity} is unset, so stale keep-alive sockets
     * are reused after Camunda pod restarts or idle timeouts and surface as
     * {@code NoHttpResponseException}. Do not set a 30s response timeout here: the
     * long-poll {@code asyncResponseTimeout} is 29500ms and would race it (EXC-CS-105).
     */
    /**
     * Topics whose handlers are batch dispatchers. They either sleep on the subscription thread
     * via {@code BaseExternalTaskHandler.throttle()} once a batch exceeds 25, or they process very
     * large batches. Isolating them stops a paced batch stalling pickup for case driven work.
     *
     * <p>Measured in production over 7 days: AUTOMATED_HEARING_NOTICE sleeps roughly 347 minutes a
     * week, BUNDLE_CREATION_CHECK 185 and HEARING_CVP_LINK 178. HEARING_FEE_CHECK does not sleep
     * but runs a batch capped at 10,000 for about 13 minutes daily.
     *
     * <p>Anything absent from this set routes to the case driven client, which is what every topic
     * gets today, so adding a subscription cannot silently change how an existing one is served.
     */
    private static final Set<String> SCHEDULER_TOPICS = Set.of(
        "AUTOMATED_HEARING_NOTICE",
        "BUNDLE_CREATION_CHECK",
        "CLAIM_DETAILS_NOTIFICATION_DEADLINE",
        "CLAIM_DISMISSED_DEADLINE",
        "DEFENDANT_RESPONSE_DEADLINE_CHECK",
        "EVIDENCE_UPLOAD_CHECK",
        "FULL_ADMIT_PAY_IMMEDIATELY_NO_PAYMENT_CHECK",
        "GenerateCsvAndSendToMmt",
        "GenerateJsonAndSendToMmt",
        "HEARING_CVP_LINK",
        "HEARING_FEE_CHECK",
        "HEARING_READINESS_CHECK",
        "INCIDENT_RETRY_EVENT",
        "MANAGE_STAY_WA_TASK_SCHEDULER",
        "MIGRATE_CASES_EVENTS",
        "MOVE_TO_DECISION_OUTCOME",
        "ORDER_REVIEW_OBLIGATION_CHECK",
        "POLLING_EVENT_EMITTER",
        "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECK",
        "RETRIGGER_CASES_EVENTS",
        "RETRIGGER_UPDATE_LOCATION_EVENTS",
        "SETTLEMENT_NO_RESPONSE_FROM_DEFENDANT_CHECK",
        "TAKE_CASE_OFFLINE",
        "TRIAL_READY_CHECK",
        "TRIAL_READY_NOTIFICATION_CHECK",
        "TRIGGER_SCHEDULER"
    );

    /**
     * The client every listener injects. Routes each subscription to one of the two real clients
     * below, so none of the 39 listeners needs to know there is more than one.
     */
    @Bean
    @Primary
    public ExternalTaskClient client(
        @Qualifier("caseDrivenExternalTaskClient") ExternalTaskClient caseDrivenClient,
        @Qualifier("schedulerExternalTaskClient") ExternalTaskClient schedulerClient) {
        return new TopicRoutingExternalTaskClient(caseDrivenClient, schedulerClient, SCHEDULER_TOPICS);
    }

    @Bean("caseDrivenExternalTaskClient")
    public ExternalTaskClient caseDrivenExternalTaskClient() {
        return buildClient(newBackoffStrategy());
    }

    @Bean("schedulerExternalTaskClient")
    public ExternalTaskClient schedulerExternalTaskClient() {
        return buildClient(newBackoffStrategy());
    }

    private ExternalTaskClient buildClient(BackoffStrategy externalTaskBackoffStrategy) {
        return ExternalTaskClient.create()
            .addInterceptor(new ServiceAuthProvider())
            .asyncResponseTimeout(eventProperties.getResponseTimeout())
            .maxTasks(1)
            .backoffStrategy(externalTaskBackoffStrategy)
            .lockDuration(eventProperties.getLockDuration()) //wait for some time to finish task before it gets picked by another client
            .baseUrl(baseUrl)
            .customizeHttpClient(httpClientBuilder -> httpClientBuilder
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setValidateAfterInactivity(TimeValue.ofMilliseconds(
                            eventProperties.getHttpValidateAfterInactivityMs()))
                        .setConnectTimeout(Timeout.ofMilliseconds(5000))
                        .build())
                    .build())
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(10))
                .addResponseInterceptorLast(new NonJsonCamundaErrorResponseInterceptor())
                .setRetryStrategy(new CamundaStaleConnectionRetryStrategy()))
            .build();
    }

    public class ServiceAuthProvider implements ClientRequestInterceptor {

        @Override
        public void intercept(ClientRequestContext requestContext) {
            requestContext.addHeader(ServiceAuthFilter.AUTHORISATION, authTokenGenerator.generate());
        }
    }
}
