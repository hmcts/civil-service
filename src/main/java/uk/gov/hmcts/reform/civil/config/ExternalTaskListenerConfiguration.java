package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

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
     * {@code EngineClientException} (EXC-CS-020). {@link ExponentialBackoffStrategy} resets to a
     * zero wait as soon as a fetch succeeds, so healthy task pickup latency is unaffected.
     */
    @Bean
    public BackoffStrategy externalTaskBackoffStrategy() {
        return new ExponentialBackoffStrategy(
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
    @Bean
    public ExternalTaskClient client(BackoffStrategy externalTaskBackoffStrategy) {
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
