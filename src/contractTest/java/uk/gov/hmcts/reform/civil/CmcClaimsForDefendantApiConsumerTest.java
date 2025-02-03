package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.civil.CmcConsumerTestUtil.buildBundleCreateResponseDsl;
import static uk.gov.hmcts.reform.cmc.model.ClaimState.OPEN;

@PactTestFor(providerName = "cmc_defendant")
@TestPropertySource(properties = "cmc-claim-store.api.url=http://localhost:6668")
@MockServerConfig(hostInterface = "localhost", port = "6668")
public class CmcClaimsForDefendantApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/claims/defendant/";
    private static final String SUBMITTER_ID_SUFFIX = "${submitterId}";
    private static final String SUBMITTER_ID = "100";

    @Autowired
    private ClaimStoreApi claimStoreApi;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getClaimsForDefendant(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildClaimsForDefendantPact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "getClaimsForDefendant")
    public void verifyClaimsForDefendant() {
        List<CmcClaim> response = claimStoreApi.getClaimsForDefendant(AUTHORIZATION_TOKEN, SUBMITTER_ID);
        assertThat(response.get(0).getState(), is(equalTo(OPEN)));
        assertThat(response.get(0).getClaimData().getAmount().getRows().get(0).getReason(), is(equalTo("No reason")));
    }

    private RequestResponsePact buildClaimsForDefendantPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Get defendant cases")
            .uponReceiving("a request for claims for a defendant")
            .pathFromProviderState(ENDPOINT + SUBMITTER_ID_SUFFIX, ENDPOINT + SUBMITTER_ID)
            .method(HttpMethod.GET.toString())
            .headers(
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN
            )
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildBundleCreateResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }
}
