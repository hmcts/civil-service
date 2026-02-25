package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "fis_hmc_api")
@MockServerConfig(hostInterface = "localhost", port = "8991")
@TestPropertySource(properties = "hmc.api.url=http://localhost:8991")
public class HearingsApiConsumerTest extends BaseContractTest {

    private static final String HEARING_ID = "2000000000000000";
    private static final String CASE_REFERENCE = "1671000000000018";
    private static final String SERVICE_CODE = "AAA7";
    private static final long CASE_ID = 1671000000000018L;
    private static final LocalDateTime HEARING_RECEIVED = LocalDateTime.of(2024, 10, 3, 11, 15);
    private static final LocalDateTime HEARING_START = LocalDateTime.of(2024, 10, 20, 9, 30);
    private static final LocalDateTime HEARING_END = HEARING_START.plusHours(2);
    private static final LocalDateTime PARTIES_NOTIFIED_DATE = LocalDateTime.of(2024, 10, 5, 14, 45);

    @Autowired
    private HearingsService hearingsService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getHearingDetails(PactDslWithProvider builder) {
        return builder
            .given("Hearing exists for supplied id")
            .uponReceiving("A request to retrieve hearing details")
            .path("/hearing/" + HEARING_ID)
            .method(HttpMethod.GET.toString())
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildGetHearingResponseBody())
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getPartiesNotified(PactDslWithProvider builder) {
        return builder
            .given("Parties notified exist for supplied hearing id")
            .uponReceiving("A request to retrieve parties notified responses")
            .path("/partiesNotified/" + HEARING_ID)
            .method(HttpMethod.GET.toString())
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildPartiesNotifiedResponseBody())
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact updatePartiesNotified(PactDslWithProvider builder) throws JSONException, IOException {
        String receivedQueryValue = "2024-10-05T14:45:00";
        return builder
            .given("Parties notified payload can be updated")
            .uponReceiving("A request to update parties notified state")
            .path("/partiesNotified/" + HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .matchQuery("version", "1", "1")
            .matchQuery("received", receivedQueryValue, receivedQueryValue)
            .body(createJsonObject(getPartiesNotifiedPayload()))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getUnnotifiedHearings(PactDslWithProvider builder) {
        String fromParam = "2024-10-01 00:00:00";
        return builder
            .given("Unnotified hearings exist for service code")
            .uponReceiving("A request to retrieve unnotified hearings")
            .path("/unNotifiedHearings/" + SERVICE_CODE)
            .method(HttpMethod.GET.toString())
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .matchQuery("hearing_start_date_from", fromParam, fromParam)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildUnnotifiedHearingsBody())
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getHearings(PactDslWithProvider builder) {
        return builder
            .given("Hearings exist for case id")
            .uponReceiving("A request to retrieve hearings for case")
            .path("/hearings/" + CASE_ID)
            .method(HttpMethod.GET.toString())
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .matchQuery("status", "LISTED", "LISTED")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildHearingsBody())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearingDetails")
    void shouldRetrieveHearingDetails() {
        HearingGetResponse response = hearingsService.getHearingResponse(AUTHORIZATION_TOKEN, HEARING_ID);

        assertThat(response.getCaseDetails().getCaseRef()).isEqualTo(CASE_REFERENCE);
        assertThat(response.getRequestDetails().getVersionNumber()).isEqualTo(1L);
        assertThat(response.getHearingResponse().getHearingDaySchedule()).hasSize(1);
    }

    @Test
    @PactTestFor(pactMethod = "getPartiesNotified")
    void shouldRetrievePartiesNotified() {
        PartiesNotifiedResponses response = hearingsService.getPartiesNotifiedResponses(AUTHORIZATION_TOKEN, HEARING_ID);

        assertThat(response.getResponses()).hasSize(1);
        assertThat(response.getResponses().get(0).getServiceData().isHearingNoticeGenerated()).isFalse();
    }

    @Test
    @PactTestFor(pactMethod = "updatePartiesNotified")
    void shouldUpdatePartiesNotified() {
        ResponseEntity<?> response = hearingsService.updatePartiesNotifiedResponse(
            AUTHORIZATION_TOKEN,
            HEARING_ID,
            1,
            PARTIES_NOTIFIED_DATE,
            getPartiesNotifiedPayload()
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    @PactTestFor(pactMethod = "getUnnotifiedHearings")
    void shouldRetrieveUnnotifiedHearings() {
        UnNotifiedHearingResponse response = hearingsService.getUnNotifiedHearingResponses(
            AUTHORIZATION_TOKEN,
            SERVICE_CODE,
            LocalDateTime.of(2024, 10, 1, 0, 0),
            null
        );

        assertThat(response.getHearingIds()).containsExactly(HEARING_ID);
        assertThat(response.getTotalFound()).isEqualTo(1L);
    }

    @Test
    @PactTestFor(pactMethod = "getHearings")
    void shouldRetrieveHearings() {
        HearingsResponse response = hearingsService.getHearings(AUTHORIZATION_TOKEN, CASE_ID, "LISTED");

        assertThat(response.getCaseRef()).isEqualTo(CASE_REFERENCE);
        assertThat(response.getCaseHearings()).hasSize(1);
        assertThat(response.getCaseHearings().get(0).getHmcStatus()).isEqualTo("LISTED");
    }

    private PartiesNotified getPartiesNotifiedPayload() {
        return new PartiesNotified()
            .setServiceData(new PartiesNotifiedServiceData()
                .setHearingNoticeGenerated(false)
                .setHearingDate(LocalDate.of(2024, 10, 20).atStartOfDay())
                .setHearingLocation("Central Court")
                .setDays(List.of(new HearingDay()
                    .setHearingStartDateTime(HEARING_START)
                    .setHearingEndDateTime(HEARING_END))));
    }

    private DslPart buildGetHearingResponseBody() {
        return newJsonBody(body ->
            body.object("requestDetails", request ->
                request.stringType("hearingRequestID", HEARING_ID)
                    .integerType("versionNumber", 1)
                    .stringType("status", "LISTED")
                    .stringType("timestamp", "2024-10-01T10:00:00"))
                .object("caseDetails", details ->
                    details.stringType("caseRef", CASE_REFERENCE)
                        .stringType("hmctsServiceCode", SERVICE_CODE))
                .object("hearingResponse", response ->
                    response.stringType("listAssistTransactionID", "TRANSACTION-123")
                        .stringType("receivedDateTime", HEARING_RECEIVED.toString())
                        .stringValue("laCaseStatus", "LISTED")
                        .stringValue("listingStatus", "FIXED")
                        .minArrayLike("hearingDaySchedule", 1, day ->
                            day.stringType("hearingStartDateTime", HEARING_START.toString())
                                .stringType("hearingEndDateTime", HEARING_END.toString())
                                .stringType("hearingVenueId", "0001")
                                .stringType("hearingRoomId", "Room A")))
                .minArrayLike("partyDetails", 1, party ->
                    party.stringType("partyID", "P1")
                        .stringType("partyName", "John Smith")
                        .stringType("partyRole", "CLAIMANT"))
        ).build();
    }

    private DslPart buildPartiesNotifiedResponseBody() {
        return newJsonBody(body ->
            body.stringType("hearingID", HEARING_ID)
                .minArrayLike("responses", 1, response ->
                    response.stringType("responseReceivedDateTime", PARTIES_NOTIFIED_DATE.toString())
                        .integerType("requestVersion", 1)
                        .stringType("partiesNotified", PARTIES_NOTIFIED_DATE.minusHours(1).toString())
                        .object("serviceData", data ->
                            data.booleanType("hearingNoticeGenerated", false)
                                .stringType("hearingLocation", "Central Court")
                                .stringType("hearingDate", HEARING_START.toString())
                                .minArrayLike("days", 1, day ->
                                    day.stringType("hearingStartDateTime", HEARING_START.toString())
                                        .stringType("hearingEndDateTime", HEARING_END.toString()))))
        ).build();
    }

    private DslPart buildUnnotifiedHearingsBody() {
        return newJsonBody(body -> {
            body.array("hearingIds", array -> array.stringValue(HEARING_ID));
            body.numberValue("totalFound", 1);
        }).build();
    }

    private DslPart buildHearingsBody() {
        return newJsonBody(body -> {
            body.stringType("hmctsServiceCode", SERVICE_CODE)
                .stringType("caseRef", CASE_REFERENCE)
                .minArrayLike("caseHearings", 1, hearing -> {
                    hearing.numberType("hearingID", 1000000000000000L)
                        .stringType("hearingRequestDateTime", "2024-10-02T08:30:00")
                        .stringType("hmcStatus", "LISTED")
                        .integerType("requestVersion", 1)
                        .minArrayLike("hearingDaySchedule", 1, day -> {
                            day.stringType("hearingStartDateTime", HEARING_START.toString())
                                .stringType("hearingEndDateTime", HEARING_END.toString());
                        });
                });
        }).build();
    }
}
