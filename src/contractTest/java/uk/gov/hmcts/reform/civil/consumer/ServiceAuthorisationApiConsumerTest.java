package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.AuthorisationService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "idam-s2s-auth")
@MockServerConfig(hostInterface = "localhost", port = "6679")
@TestPropertySource(properties = {
    "idam.s2s-auth.url=http://localhost:6679",
    "idam.s2s-auth.microservice=civil_service",
    "idam.s2s-auth.totp_secret=AABBCCDDEEFFGGHH",
    "civil.authorised-services=civil_service"
})
public class ServiceAuthorisationApiConsumerTest extends BaseContractTest {

    private static final String MICRO_SERVICE = "civil_service";
    private static final String ONE_TIME_PASSWORD = "123456";
    private static final String SERVICE_TOKEN = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJjaXZpbF9zZXJ2aWNlIn0.";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Autowired
    private AuthorisationService authorisationService;

    @Pact(consumer = "civil_service")
    public RequestResponsePact generateServiceToken(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a service token lease request")
            .path("/lease")
            .method(HttpMethod.POST.toString())
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildServiceTokenRequest())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body(SERVICE_TOKEN)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getServiceName(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a service name details request")
            .path("/details")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body(MICRO_SERVICE)
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateServiceToken")
    public void verifyGenerateServiceToken() {
        String serviceToken = authTokenGenerator.generate();

        assertThat(serviceToken, is("Bearer " + SERVICE_TOKEN));
    }

    @Test
    @PactTestFor(pactMethod = "getServiceName")
    public void verifyGetServiceName() {
        String serviceName = serviceAuthorisationApi.getServiceName(AUTHORIZATION_TOKEN);

        assertThat(serviceName, is(MICRO_SERVICE));
        assertThat(authorisationService.isServiceAuthorized(AUTHORIZATION_TOKEN), is(true));
    }

    private DslPart buildServiceTokenRequest() {
        return LambdaDsl.newJsonBody(root -> root
            .stringValue("microservice", MICRO_SERVICE)
            .stringMatcher("oneTimePassword", "\\d{6}", ONE_TIME_PASSWORD)
        ).build();
    }
}
