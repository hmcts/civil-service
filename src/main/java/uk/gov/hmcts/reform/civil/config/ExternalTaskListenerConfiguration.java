package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Configuration
public class ExternalTaskListenerConfiguration {

    private final String baseUrl;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public ExternalTaskListenerConfiguration(@Value("${feign.client.config.remoteRuntimeService.url}") String baseUrl,
                                             AuthTokenGenerator authTokenGenerator) {
        this.baseUrl = baseUrl;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Bean
    public ExternalTaskClient client() {
        return ExternalTaskClient.create()
            .addInterceptor(new ServiceAuthProvider())
            .asyncResponseTimeout(29000)
            .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0))
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
