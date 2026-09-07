package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.JudicialRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.JudgeRefData;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "referenceData_judicialv2")
@MockServerConfig(hostInterface = "localhost", port = "6683")
@TestPropertySource(properties = {
    "genApp.jrd.url=http://localhost:6683",
    "genApp.jrd.endpoint=/refdata/judicial/users/search"
})
public class JudicialRefDataApiConsumerTest extends BaseContractTest {

    private static final String JRD_V2_MEDIA_TYPE = "application/vnd.jrd.api+json;Version=2.0";
    private static final String SEARCH_STRING = "Smith";
    private static final String PERSONAL_CODE = "1234";

    @Autowired
    private JudicialRefDataService judicialRefDataService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact searchJudicialUsers(PactDslWithProvider builder) {
        return builder
            .given("return judicial user profiles")
            .uponReceiving("a judicial ref data user search request")
            .path("/refdata/judicial/users/search")
            .headers(
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
            )
            .method(HttpMethod.POST.toString())
            .body("""
                {
                  "searchString": "%s",
                  "serviceCode": null,
                  "location": null
                }
                """.formatted(SEARCH_STRING))
            .willRespondWith()
            .matchHeader(
                HttpHeaders.CONTENT_TYPE,
                "application/(json|vnd\\.jrd\\.api\\+json;Version=2\\.0)(;.*)?",
                JRD_V2_MEDIA_TYPE
            )
            .body(buildJudicialUsersResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchJudicialUsers")
    public void verifySearchJudicialUsers() {
        List<JudgeRefData> response = judicialRefDataService.getJudgeReferenceData(SEARCH_STRING, AUTHORIZATION_TOKEN);

        assertThat(response.size(), is(equalTo(1)));
        assertThat(response.get(0).getPersonalCode(), is(equalTo(PERSONAL_CODE)));
    }

    private DslPart buildJudicialUsersResponse() {
        return newJsonArray(root -> root.object(judge -> judge
            .stringType("title", "Family Judge")
            .stringType("knownAs", "testKnownAs")
            .stringType("surname", "surname")
            .stringType("fullName", "testFullName")
            .stringType("emailId", "test@test.com")
            .stringType("idamId", "44362987-3fe4-43b3-b59e-91c46b6b1fd4")
            .stringType("personalCode", PERSONAL_CODE)
            .stringType("postNominals", "testPostNominals")
            .stringType("initials", "I N"))).build();
    }
}
