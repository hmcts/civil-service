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

@PactTestFor(providerName = "cmc_claimant")
@TestPropertySource(properties = "cmc-claim-store.api.url=http://localhost:6676")
@MockServerConfig(hostInterface = "localhost", port = "6676")
public class CmcClaimsForClaimantApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/claims/claimant/";
    private static final String SUBMITTER_ID_SUFFIX = "${submitterId}";
    private static final String SUBMITTER_ID = "100";

    @Autowired
    private ClaimStoreApi claimStoreApi;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getClaimsForClaimant(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildClaimsForClaimantPact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "getClaimsForClaimant")
    public void verifyClaimsForClaimant() {
        List<CmcClaim> response = claimStoreApi.getClaimsForClaimant(AUTHORIZATION_TOKEN, SUBMITTER_ID);
        assertThat(response.get(0).getState(), is(equalTo(OPEN)));
        assertThat(response.get(0).getClaimData().getAmount().getRows().get(0).getReason(), is(equalTo("No reason")));
    }

    private RequestResponsePact buildClaimsForClaimantPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Get claimant cases")
            .uponReceiving("a request for claims for a claimant")
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
