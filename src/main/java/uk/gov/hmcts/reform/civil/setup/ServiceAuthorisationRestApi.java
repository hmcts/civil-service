package uk.gov.hmcts.reform.civil.setup;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import java.util.Map;

final class ServiceAuthorisationRestApi implements ServiceAuthorisationApi {

    private final String serviceAuthBaseUrl;
    private final RestTemplate restTemplate;

    ServiceAuthorisationRestApi(String serviceAuthBaseUrl, RestTemplate restTemplate) {
        this.serviceAuthBaseUrl = serviceAuthBaseUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public String serviceToken(Map<String, String> signIn) {
        try {
            return restTemplate.postForObject(serviceAuthBaseUrl + "/lease", signIn, String.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Failed to lease service token for Camunda import", exception);
        }
    }

    @Override
    public void authorise(String authHeader, String[] roles) {
        throw new UnsupportedOperationException("authorise is not required for Camunda import");
    }

    @Override
    public String getServiceName(String authHeader) {
        throw new UnsupportedOperationException("getServiceName is not required for Camunda import");
    }
}
