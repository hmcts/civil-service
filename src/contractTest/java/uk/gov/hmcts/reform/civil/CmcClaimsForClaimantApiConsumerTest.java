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
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.io.IOException;
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.cmc.model.ClaimState.OPEN;

@PactTestFor(providerName = "cmc_claimant")
@TestPropertySource(properties = "cmc-claim-store.api.url=http://localhost:6669")
@MockServerConfig(hostInterface = "localhost", port = "6669")
public class CmcClaimsForClaimantApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/claims/claimant/";
    private static final String SUBMITTER_ID_SUFFIX = "${submitterId}";
    private static final String SUBMITTER_ID = "100";

    @Autowired
    private ClaimStoreApi claimStoreApi;

    @Pact(consumer = "civil-service")
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

    static DslPart buildBundleCreateResponseDsl() {
        return newJsonArray(response -> response
            .object(cmcClaim -> cmcClaim
                .stringType("submitterId", "123")
                .stringType("letterHolderId", "letterHolderId")
                .stringType("defendantId", "defendantId")
                .stringType("externalId", "externalId")
                .stringType("referenceNumber", "referenceNumber")
                .object("claim", claimData ->
                    claimData
                        .object("amount", amount ->
                            amount
                                .minArrayLike("rows", 1, rows ->
                                    rows
                                        .stringType("eason", "No reason")
                                        .numberType("amount", 20.0)))
                        .minArrayLike("claimants", 1, claimants ->
                            claimants
                                .stringType("name", "name")
                                .object("address", address ->
                                    address
                                        .stringType("line1", "line1")
                                        .stringType("line2", "line2")
                                        .stringType("line3", "line3")
                                        .stringType("city", "city")
                                        .stringType("county", "county")
                                        .stringType("postcode", "postcode"))
                                .object("correspondenceAddress", address ->
                                    address
                                        .stringType("line1", "line1")
                                        .stringType("line2", "line2")
                                        .stringType("line3", "line3")
                                        .stringType("city", "city")
                                        .stringType("county", "county")
                                        .stringType("postcode", "postcode"))
                                .object("breathingSpace", breathingSpace ->
                                    breathingSpace
                                        .stringType("bsReferenceNumber", "bsReferenceNumber")
                                        .date("bsEnteredDate", "yyyy-MM-dd")
                                        .date("bsLiftedDate", "yyyy-MM-dd")
                                        .date("bsEnteredDateByInsolvencyTeam", "yyyy-MM-dd")
                                        .date("bsLiftedDateByInsolvencyTeam", "yyyy-MM-dd")
                                        .date("bsExpectedEndDate", "yyyy-MM-dd")
                                        .stringType("bsLiftedFlag", "bsLiftedFlag")
                                )
                        )
                )
                .date("responseDeadline", "yyyy-MM-dd")
                .booleanType("moreTimeRequested")
                .stringType("submitterEmail", "submitterEmail")
                .object("response", response1 -> response1
                    .stringType("responseType", "FULL_DEFENCE")
                    .object("paymentIntention", paymentIntention ->
                        paymentIntention
                            .stringType("paymentOption", "IMMEDIATELY")
                            .date("paymentDate", "yyyy-MM-dd"))
                    .object("paymentDeclaration", paymentDeclaration ->
                        paymentDeclaration
                            .date("paidDate", "yyyy-MM-dd")
                            .numberType("paidAmount", 10.0))
                    .stringType("responseMethod", "OFFLINE"))
                .date("moneyReceivedOn", "yyyy-MM-dd")
                .date("countyCourtJudgmentRequestedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("createdAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("reDeterminationRequestedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("admissionPayImmediatelyPastPaymentDate", "yyyy-MM-dd")
                .date("intentionToProceedDeadline", "yyyy-MM-dd")
                .date("claimantRespondedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .object("claimantResponse", claimantResponse ->
                    claimantResponse
                        .stringType("type", "ACCEPTATION")
                        .numberType("amountPaid", 50.0)
                        .stringType("paymentReceived", "paymentReceived")
                        .stringType("settleForAmount", "settleForAmount")
                        .object("courtDetermination", courtDetermination ->
                            courtDetermination
                                .object("courtDecision", courtDecision ->
                                    courtDecision
                                        .stringType("paymentOption", "IMMEDIATELY")
                                        .date("paymentDate", "yyyy-MM-dd"))
                                .object("courtPaymentIntention", courtDecision ->
                                    courtDecision
                                        .stringType("paymentOption", "IMMEDIATELY")
                                        .date("paymentDate", "yyyy-MM-dd"))
                                .stringType("rejectionReason", "rejectionReason")
                                .numberType("disposableIncome", 30.0))
                        .stringType("formaliseOption", "SETTLEMENT"))
                .stringType("state", "OPEN")
                .stringType("proceedOfflineReason", "OTHER")
                .object("settlement", settlement ->
                    settlement
                        .minArrayLike("partyStatements", 1, partyStatements ->
                            partyStatements
                                .stringType("type", "OFFER")
                                .stringType("madeBy", "CLAIMANT")
                                .object("offer", offer ->
                                    offer
                                        .stringType("content", "content")
                                        .date("completionDate", "yyyy-MM-dd")
                                        .object("paymentIntention", paymentIntention ->
                                            paymentIntention
                                                .stringType("paymentOption", "IMMEDIATELY")
                                                .date("paymentDate", "yyyy-MM-dd")))))
            )).build();
    }
}
