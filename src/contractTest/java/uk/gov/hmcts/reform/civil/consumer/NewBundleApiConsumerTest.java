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
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "em_newBundle")
@MockServerConfig(hostInterface = "localhost", port = "6663")
@TestPropertySource(properties = "bundle.api.url=http://localhost:6663")
public class NewBundleApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/api/new-bundle";

    @Autowired
    private EvidenceManagementApiClient evidenceManagementApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact postCreateBundleServiceRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCreateBundleResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "postCreateBundleServiceRequest")
    public void verifyNewBundle() {
        BundleCreateResponse response = evidenceManagementApiClient.createNewBundle(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            getBundleCreateRequest()
        );
        assertThat(response.getDocumentTaskId(), is(equalTo(123)));
        assertThat(
            response.getData().getCaseBundles().get(0).getValue().getStitchedDocument().getDocumentUrl(),
            is(equalTo("documentStitchedUrl"))
        );
    }

    private RequestResponsePact buildCreateBundleResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a new bundle request")
            .path(ENDPOINT)
            .method(HttpMethod.POST.toString())
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(getBundleCreateRequest()))
            .willRespondWith()
            .body(buildBundleCreateResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private BundleCreateRequest getBundleCreateRequest() {

        return new BundleCreateRequest()
            .setCaseTypeId("caseTypeId")
            .setJurisdictionId("jurisdictionId")
            .setCaseDetails(new BundlingCaseDetails()
                             .setCaseData(new BundlingCaseData()
                                           .setBundleConfiguration("bundleConfiguration")
                                           .setId(666)
                                           .setTrialDocuments(List.of(getTestElement()))
                                           .setStatementsOfCaseDocuments(List.of(getTestElement()))
                                           .setDirectionsQuestionnaires(List.of(getTestElement()))
                                           .setParticularsOfClaim(List.of(getTestElement()))
                                           .setOrdersDocuments(List.of(getTestElement()))
                                           .setClaimant1WitnessStatements(List.of(getTestElement()))
                                           .setClaimant2WitnessStatements(List.of(getTestElement()))
                                           .setDefendant1WitnessStatements(List.of(getTestElement()))
                                           .setDefendant2WitnessStatements(List.of(getTestElement()))
                                           .setClaimant1ExpertEvidence(List.of(getTestElement()))
                                           .setClaimant2ExpertEvidence(List.of(getTestElement()))
                                           .setDefendant1ExpertEvidence(List.of(getTestElement()))
                                           .setDefendant2ExpertEvidence(List.of(getTestElement()))
                                           .setJointStatementOfExperts(List.of(getTestElement()))
                                           .setClaimant1DisclosedDocuments(List.of(getTestElement()))
                                           .setClaimant2DisclosedDocuments(List.of(getTestElement()))
                                           .setDefendant1DisclosedDocuments(List.of(getTestElement()))
                                           .setDefendant2DisclosedDocuments(List.of(getTestElement()))
                                           .setClaimant1CostsBudgets(List.of(getTestElement()))
                                           .setClaimant2CostsBudgets(List.of(getTestElement()))
                                           .setDefendant1CostsBudgets(List.of(getTestElement()))
                                           .setDefendant2CostsBudgets(List.of(getTestElement()))
                                           .setSystemGeneratedCaseDocuments(List.of(getTestElement()))
                                           .setApplicant1(getParty("applicant1"))
                                           .setHasApplicant2(true)
                                           .setApplicant2(getParty("applicant2"))
                                           .setRespondent1(getParty("respondent1"))
                                           .setHasRespondant2(true)
                                           .setRespondent2(getParty("respondent2"))
                                           .setHearingDate("2002-01-01")
                                           .setCcdCaseReference(66666L))
                             .setFilenamePrefix("filenamePrefix"))
            .setEventId("eventId");
    }

    private Party getParty(String applicant) {
        return Party.builder()
            .partyID("partyID")
            .type(Party.Type.COMPANY)
            .individualTitle("Mr")
            .individualFirstName(applicant)
            .individualLastName("Silvassauro")
            .individualDateOfBirth(LocalDate.of(2019, 1, 1))
            .companyName("company")
            .organisationName("org")
            .soleTraderTitle("soleTr")
            .soleTraderFirstName("soleTrFN")
            .soleTraderLastName("soleTrLN")
                .soleTraderDateOfBirth(LocalDate.of(2019, 1, 1))
            .primaryAddress(getTestAddress("rua1"))
            .partyName(applicant)
            .bulkClaimPartyName("bulk")
            .partyTypeDisplayValue("typeDispl")
            .partyEmail("is@is.is")
            .partyPhone("07070006066")
            .legalRepHeading("legalRep")
            .unavailableDates(getUnavailableTestDates())
            .flags(new Flags())
            .build();
    }

    private List<Element<UnavailableDate>> getUnavailableTestDates() {
        return List.of(Element.<UnavailableDate>builder().id(UUID.fromString("00e5384f-03b3-4634-8b67-6acb665e83ba"))
                           .value(new UnavailableDate()
                                      .setWho("who")
                                      .setDate(LocalDate.of(2020, 1, 1))
                                      .setFromDate(LocalDate.of(2020, 1, 1))
                                      .setToDate(LocalDate.of(2020, 1, 1))
                                      .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                                      .setEventAdded("eventAdd")
                                      .setDateAdded(LocalDate.of(2020, 1, 1))).build());
    }

    private Address getTestAddress(String rua) {
        Address address = new Address();
        address.setAddressLine1(rua);
        address.setAddressLine2(rua);
        address.setAddressLine3(rua);
        address.setPostTown("town");
        address.setCountry("UK");
        address.setCounty("Shire");
        address.setPostCode("KT1 3ER");
        return address;
    }

    private Element<BundlingRequestDocument> getTestElement() {
        return Element.<BundlingRequestDocument>builder()
            .id(UUID.fromString("00e5384f-03b3-4634-8b67-6acb665e83ba"))
            .value(new BundlingRequestDocument()
                       .setDocumentFileName("docFileName")
                       .setDocumentLink(new DocumentLink()
                                         .setDocumentBinaryUrl("binaryUrl")
                                         .setDocumentFilename("docFileName")
                                         .setDocumentUrl("docURL"))
                       .setDocumentType("testDocType"))
            .build();
    }

    static DslPart buildBundleCreateResponseDsl() {
        return newJsonBody(response ->
                               response
                                   .numberType("documentTaskId", 123)
                                   .object("data", bundleData ->
                                       bundleData
                                           .minArrayLike("caseBundles", 1, caseBundle ->
                                               caseBundle
                                                   .object("value", bundleDetails ->
                                                       bundleDetails
                                                           .stringType("id", "id")
                                                           .stringType("title", "title")
                                                           .stringType("description", "description")
                                                           .stringType("stitchStatus", "stitchStatus")
                                                           .stringType("fileName", "fileName")
                                                           .stringMatcher("createdOn",
                                                                          "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{1,6})$",
                                                                          "2020-10-06T18:54:48.785000")
                                                           .date("bundleHearingDate", "yyyy-MM-dd")
                                                           .object("stitchedDocument", stitchedDocument ->
                                                               stitchedDocument
                                                                   .stringType("document_url", "documentStitchedUrl")
                                                                   .stringType(
                                                                       "document_binary_url",
                                                                       "documentBinaryUrl"
                                                                   )
                                                                   .stringType("document_filename", "documentFileName")
                                                                   .stringType("document_hash", "documentHash")
                                                                   .stringType("category_id", "categoryID")
                                                           )
                                                   )
                                           )
                                   )
        ).build();
    }

}
