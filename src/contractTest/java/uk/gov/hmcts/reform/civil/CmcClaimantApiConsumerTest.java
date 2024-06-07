package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.civil.client.BundleApiClient;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.cmc.model.ClaimState.OPEN;

@PactTestFor(providerName = "cmc_claimant", port = "8765")
public class CmcClaimantApiConsumerTest extends BaseContractTest{

    public static final String ENDPOINT = "/claims/claimant/";
    private static final String SUBMITTER_ID = "someId";

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
        List<CmcClaim> response = claimStoreApi.getClaimsForClaimant(AUTHORIZATION_HEADER, SUBMITTER_ID);
        assertThat(response.get(0).getState(), is(equalTo(OPEN)));
        assertThat(response.get(0).getClaimData().getAmount().getRows().get(0).getReason(), is(equalTo("No reason")));
    }

    private RequestResponsePact buildClaimsForClaimantPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("There are claims for the claimant")
            .uponReceiving("a request for claims for a claimant")
            .path(ENDPOINT)
            .method(HttpMethod.GET)
            .headers(
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN
            )
            .matchPath(SUBMITTER_ID)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildBundleCreateResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    static DslPart buildBundleCreateResponseDsl() {
        return newJsonBody(response ->
                               response
                                   .stringType("submitterId", "123")
                                   .stringType("letterHolderId", "letterHolderId")
                                   .stringType("defendantId", "defendantId")
                                   .stringType("externalId", "externalId")
                                   .stringType("referenceNumber", "referenceNumber")
                                   .numberType("totalAmountTillToday", 10.0)

                                   .object("claim", claimData ->
                                       claimData
                                           .object("amount", amount ->
                                               amount
                                                   .minArrayLike("rows", 1, rows ->
                                                       rows
                                                           .stringType("reason", "No reason")
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
                                       .stringType("responseType", "responseType")
                                       .object("paymentIntention", paymentIntention ->
                                           paymentIntention
                                               .stringType("paymentOption", "Immediately")
                                               .date("paymentDate", "yyyy-MM-dd"))
                                       .object("paymentDeclaration", paymentDeclaration ->
                                           paymentDeclaration
                                               .date("paidDate", "yyyy-MM-dd")
                                               .numberType("paidAmount", 10.0))
                                       .stringType("responseMethod", "responseMethod"))
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
                                                           .stringType("paymentOption", "paymentOption")
                                                           .date("paymentDate", "yyyy-MM-dd"))
                                                   .object("courtPaymentIntention", courtDecision ->
                                                       courtDecision
                                                           .stringType("paymentOption", "paymentOption")
                                                           .date("paymentDate", "yyyy-MM-dd"))
                                                   .stringType("rejectionReason", "rejectionReason")
                                                   .numberType("disposableIncome", 30.0))
                                           .stringType("formaliseOption", "Settlement"))
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
                                                                   .stringType("paymentOption", "paymentOption")
                                                                   .date("paymentDate", "yyyy-MM-dd")))))
        ).build();
    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
