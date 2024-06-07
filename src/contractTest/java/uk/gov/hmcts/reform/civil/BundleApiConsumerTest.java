package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "em_newBundle", port = "8084")
@ExtendWith(PactConsumerTestExt.class)
@ActiveProfiles("integration-test")
@SpringBootTest
public class BundleApiConsumerTest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    public static final String ENDPOINT = "/api/new-bundle";

    @Autowired
    private BundleApiClient bundleApiClient;
    private ObjectMapper objectMapper = JsonMapper
        .builder()
        .addModule(new JavaTimeModule())
        .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    @Pact(consumer = "civil-service")
    public RequestResponsePact postCreateBundleServiceRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildCreateBundleResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "postCreateBundleServiceRequest")
    public void verifyNewBundle() throws IOException {
        String json = createJsonObject(getBundleCreateRequest());
        BundleCreateResponse response = bundleApiClient.createBundleServiceRequest(
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
            .given("There are documents to be bundled")
            .uponReceiving("a new bundle request")
            .path(ENDPOINT)
            .method(HttpMethod.POST)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(getBundleCreateRequest()))
            .willRespondWith()
            .body(buildBundleCreateResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private BundleCreateRequest getBundleCreateRequest() {
        return BundleCreateRequest.builder()
            .caseTypeId("caseTypeId")
            .jurisdictionId("jurisdictionId")
            .caseDetails(BundlingCaseDetails.builder()
                             .caseData(BundlingCaseData.builder()
                                           .bundleConfiguration("bundleConfiguration")
                                           .id(666)
                                           /* .trialDocuments(List.of(getTestElement()))
                                            .statementsOfCaseDocuments(List.of(getTestElement()))
                                           .directionsQuestionnaires(List.of(getTestElement()))
                                           .particularsOfClaim(List.of(getTestElement()))
                                           .ordersDocuments(List.of(getTestElement()))
                                           .claimant1WitnessStatements(List.of(getTestElement()))
                                           .claimant2WitnessStatements(List.of(getTestElement()))
                                           .defendant1WitnessStatements(List.of(getTestElement()))
                                           .defendant2WitnessStatements(List.of(getTestElement()))
                                           .claimant1ExpertEvidence(List.of(getTestElement()))
                                           .claimant2ExpertEvidence(List.of(getTestElement()))
                                           .defendant1ExpertEvidence(List.of(getTestElement()))
                                           .defendant2ExpertEvidence(List.of(getTestElement()))
                                           .jointStatementOfExperts(List.of(getTestElement()))
                                           .claimant1DisclosedDocuments(List.of(getTestElement()))
                                           .claimant2DisclosedDocuments(List.of(getTestElement()))
                                           .defendant1DisclosedDocuments(List.of(getTestElement()))
                                           .defendant2DisclosedDocuments(List.of(getTestElement()))
                                           .claimant1CostsBudgets(List.of(getTestElement()))
                                           .claimant2CostsBudgets(List.of(getTestElement()))
                                           .defendant1CostsBudgets(List.of(getTestElement()))
                                           .defendant2CostsBudgets(List.of(getTestElement()))
                                           .systemGeneratedCaseDocuments(List.of(getTestElement()))
                                          .applicant1(getParty("applicant1"))
                                            .hasApplicant2(true)
                                            .applicant2(getParty("applicant2"))
                                            .respondent1(getParty("respondent1"))
                                            .hasRespondant2(true)
                                            .respondent2(getParty("respondent2"))*/
                                           .hearingDate("2002-01-01")
                                           .ccdCaseReference(66666L)
                                           .build())
                             .filenamePrefix("filenamePrefix").build())
            .eventId("eventId").build();
    }

    private Party getParty(String applicant) {
        return Party.builder()
            .partyID(UUID.randomUUID().toString())
            .type(Party.Type.COMPANY)
            .individualTitle("Mr")
            .individualFirstName(applicant)
            .individualLastName("Silvassauro")
            .individualDateOfBirth(LocalDate.now().minusDays(10))
            .companyName("company")
            .organisationName("org")
            .soleTraderTitle("soleTr")
            .soleTraderFirstName("soleTrFN")
            .soleTraderLastName("soleTrLN")
            .soleTraderDateOfBirth(LocalDate.now().minusDays(10))
            .primaryAddress(getTestAddress("rua1"))
            .partyName(applicant)
            .bulkClaimPartyName("bulk")
            .partyTypeDisplayValue("typeDispl")
            .partyEmail("is@is.is")
            .partyPhone("07070006066")
            .legalRepHeading("legalRep")
            .unavailableDates(getUnavailableTestDates())
            .flags(Flags.builder().build())
            .build();
    }

    private List<Element<UnavailableDate>> getUnavailableTestDates() {
        return List.of(Element.<UnavailableDate>builder().id(UUID.randomUUID())
                           .value(UnavailableDate.builder()
                                      .who("who")
                                      .date(LocalDate.now())
                                      .fromDate(LocalDate.now())
                                      .toDate(LocalDate.now())
                                      .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                      .eventAdded("eventAdd")
                                      .dateAdded(LocalDate.now())
                                      .build()).build());
    }

    private Address getTestAddress(String rua) {
        return Address.builder()
            .addressLine1(rua)
            .addressLine2(rua)
            .addressLine3(rua)
            .postTown("town")
            .country("UK")
            .county("Shire")
            .postCode("KT1 3ER")
            .build();
    }

    private static Element<BundlingRequestDocument> getTestElement() {
        return Element.<BundlingRequestDocument>builder()
            .id(UUID.randomUUID())
            .value(BundlingRequestDocument.builder()
                       .documentFileName("docFileName")
                       .documentLink(DocumentLink.builder()
                                         .documentBinaryUrl("binaryUrl")
                                         .documentFilename("docFileName")
                                         .documentUrl("docURL")
                                         .build())
                       .documentType("testDocType")
                       .build())
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
                                        .date("createdOn", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                                        .date("bundleHearingDate", "yyyy-MM-dd")
                                        .object("stitchedDocument", stitchedDocument ->
                                            stitchedDocument
                                                .stringType("document_url", "documentStitchedUrl")
                                                .stringType("document_binary_url", "documentBinaryUrl")
                                                .stringType("document_filename", "documentFileName")
                                                .stringType("document_hash", "documentHash")
                                                .stringType("category_id", "categoryID")
                                        )
                                )
                        )
                )
        ).build();
    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
