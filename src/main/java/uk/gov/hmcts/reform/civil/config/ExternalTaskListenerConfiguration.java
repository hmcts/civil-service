package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
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

@Configuration
@EnableRetry
public class ExternalTaskListenerConfiguration {

    private final String baseUrl;
    private final String workerId;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public ExternalTaskListenerConfiguration(@Value("${feign.client.config.processInstance.url}") String baseUrl,
                                             @Value("${camunda.workerId}") String workerId,
                                             AuthTokenGenerator authTokenGenerator) {
        this.baseUrl = baseUrl;
        this.workerId = workerId;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Bean
    public ExternalTaskClient client() {
        return ExternalTaskClient.create()
            .workerId(workerId)
            .addInterceptor(new ServiceAuthProvider())
            .asyncResponseTimeout(29000)
            .maxTasks(1)
            .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0))
            .lockDuration(33L * 60L * 1000L) //wait for 33min to finish task before it gets picked by other client
            .baseUrl(baseUrl)
            .build();
    }

    public class ServiceAuthProvider implements ClientRequestInterceptor {

        @Override
        public void intercept(ClientRequestContext requestContext) {
            requestContext.addHeader(ServiceAuthFilter.AUTHORISATION, authTokenGenerator.generate());
        }
    }
}
