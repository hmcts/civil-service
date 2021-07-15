package uk.gov.hmcts.reform.civil.config.camunda;

import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Configuration
public class RequestInterceptorConfiguration implements ClientRequestInterceptor {

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public RequestInterceptorConfiguration(AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public void intercept(ClientRequestContext requestContext) {
        requestContext.addHeader(ServiceAuthFilter.AUTHORISATION, authTokenGenerator.generate());
    }
}
