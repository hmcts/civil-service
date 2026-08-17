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

@PactTestFor(providerName = "judicial_ref_data")
@MockServerConfig(hostInterface = "localhost", port = "6683")
@TestPropertySource(properties = {
    "genApp.jrd.url=http://localhost:6683",
    "genApp.jrd.endpoint=/refdata/judicial/users/search"
})
public class JudicialRefDataApiConsumerTest extends BaseContractTest {

    private static final String SEARCH_STRING = "Smith";
    private static final String PERSONAL_CODE = "1234567";

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
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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
            .stringValue("post_nominals", "DJ")
            .stringValue("known_as", "Jane")
            .stringValue("surname", "Smith")
            .stringValue("full_name", "District Judge Jane Smith")
            .stringValue("ejudiciary_email", "jane.smith@judiciary.uk")
            .stringValue("sidam_id", "11111111-1111-1111-1111-111111111111")
            .stringValue("personal_code", PERSONAL_CODE)
            .stringValue("is_judge", "Y")
            .stringValue("is_panel_member", "N")
            .stringValue("is_magistrate", "N"))).build();
    }
}
