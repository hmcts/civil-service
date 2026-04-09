package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.io.IOException;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "referenceData_organisationalExternalUsers")
@MockServerConfig(hostInterface = "localhost", port = "6665")
@TestPropertySource(properties = "rd_professional.api.url=http://localhost:6665")
public class ExternalOrganisationApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/refdata/external/v1/organisations";

    @Autowired
    private OrganisationApi organisationApi;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getUserOrganisation(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildUserOrganisationResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "getUserOrganisation")
    public void verifyUserOrganisation() {
        Organisation response = organisationApi.findUserOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN);

        assertThat(response.getOrganisationIdentifier(), is("BJMSDFDS80808"));
    }

    private RequestResponsePact buildUserOrganisationResponsePact(PactDslWithProvider builder) {
        return builder
            .given("Organisation with Id exists")
            .uponReceiving("A Request to get organisation for user")
            .method("GET")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER,
                     SERVICE_AUTH_TOKEN
            )
            .path(ENDPOINT)
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildOrganisationResponseDsl() {
        return newJsonBody(o -> {
            o
                .stringType("companyNumber", "companyNumber")
                .stringType("companyUrl", "companyUrl")
                .minArrayLike("contactInformation", 1, 1,
                              sh -> {
                                  sh.stringType("addressLine1", "addressLine1")
                                      .stringType("addressLine2", "addressLine2")
                                      .stringType("country", "UK")
                                      .stringType("postCode", "SM12SX");

                              }
                )
                .stringType("name", "theKCompany")
                .stringType("organisationIdentifier", "BJMSDFDS80808")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", Boolean.TRUE)
                .stringType("status", "ACTIVE")
                .object("superUser", superUser ->
                    superUser
                        .stringType("email", "email")
                        .stringType("firstName", "firstName")
                        .stringType("lastName", "lastName")
                );
        }).build();
    }
}
