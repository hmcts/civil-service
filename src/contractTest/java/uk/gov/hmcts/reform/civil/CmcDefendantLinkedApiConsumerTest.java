package uk.gov.hmcts.reform.civil;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.DefendantLinkStatus;

import java.io.IOException;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "cmc_defendantLinked")
@TestPropertySource(properties = "cmc-claim-store.api.url=http://localhost:6665")
@MockServerConfig(hostInterface = "localhost", port = "6665")
public class CmcDefendantLinkedApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT_PREFIX = "/claims/";

    public static final String ENDPOINT_SUFFIX = "/defendant-link-status";

    private static final String CASE_REFERENCE_ID_SUFFIX = "${caseReference}";

    private static final String CASE_REFERENCE = "100";

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
        DefendantLinkStatus response = claimStoreApi.isDefendantLinked(CASE_REFERENCE);
        assertThat(response.isLinked(), is(true));
    }

    private RequestResponsePact buildClaimsForDefendantPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Get claimant linked cases status")
            .uponReceiving("a request for claimant linked cases status for a case reference")
            .pathFromProviderState(
                ENDPOINT_PREFIX + CASE_REFERENCE_ID_SUFFIX + ENDPOINT_SUFFIX,
                ENDPOINT_PREFIX + CASE_REFERENCE + ENDPOINT_SUFFIX
            )
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildBundleCreateResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    static DslPart buildBundleCreateResponseDsl() {
        return newJsonBody(response ->
                               response
                                   .booleanType("linked", true)
        ).build();
    }
}
