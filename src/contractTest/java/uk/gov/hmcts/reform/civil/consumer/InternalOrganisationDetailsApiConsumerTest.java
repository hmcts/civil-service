package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "referenceData_organisationalDetailsInternal")
@MockServerConfig(hostInterface = "localhost", port = "6668")
@TestPropertySource(properties = "rd_professional.api.url=http://localhost:6668")
public class InternalOrganisationDetailsApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/refdata/internal/v1/organisations/orgDetails";
    public static final String USER_ID = "someId";

    @Autowired
    private OrganisationApi organisationApi;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getOrganisationByUserId(PactDslWithProvider builder) {
        return buildUserOrganisationResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "getOrganisationByUserId")
    public void verifyInternalOrganisation() {
        Organisation response =
            organisationApi.findOrganisationByUserId(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, USER_ID);

        assertThat(response.getOrganisationIdentifier(), is("BJMSDFDS80808"));
    }

    private RequestResponsePact buildUserOrganisationResponsePact(PactDslWithProvider builder) {
        return builder
            .given("Organisation exists for given Id")
            .uponReceiving("a request to get an organisation by id")
            .path(ENDPOINT + "/" + USER_ID)
            .method("GET")
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
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
