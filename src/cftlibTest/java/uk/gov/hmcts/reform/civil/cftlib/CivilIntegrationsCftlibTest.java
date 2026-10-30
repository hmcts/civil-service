package uk.gov.hmcts.reform.civil.cftlib;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.rse.ccd.lib.Database;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;

/**
 * Case events that run through cftlib's Camunda, HMC and work allocation, as they would in an environment.
 *
 * <p>The work allocation tests start with a hearing HMI cannot list. HMC publishes the EXCEPTION on the hmc-to-cft
 * topic, this service turns it into REVIEW_HEARING_EXCEPTION, and work allocation creates and configures a task from
 * it. The tests then submit case events that act on that task.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CivilIntegrationsCftlibTest extends CftlibTest {

    private static final String HEARING_SERVICE = "http://localhost:4561";
    private static final String SYSTEM_UPDATE = "civil-system-update@mailnesia.com";
    // The specified claims service code, which this service's hmc-to-cft subscription and handler accept.
    private static final String SERVICE_CODE = "AAA7";
    private static final Duration WAIT = Duration.ofMinutes(2);
    private static final LocalDateTime LISTED_START = LocalDate.now().plusDays(14).atTime(10, 0);

    @Autowired
    private UserService userService;
    @Autowired
    private CoreCaseDataService coreCaseDataService;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String systemUpdate;

    @BeforeAll
    void signIn() {
        systemUpdate = userService.getAccessToken(SYSTEM_UPDATE, "password");
    }

    @Test
    void caseEventRunsItsBusinessProcessInCamunda() {
        String caseReference = createCase("PREPARE_FOR_HEARING_CONDUCT_HEARING");

        coreCaseDataService.triggerEvent(Long.parseLong(caseReference), MOVE_TO_DECISION_OUTCOME, Map.of());

        // The event's callback readies the MOVE_TO_DECISION_OUTCOME business process, which this service starts in
        // Camunda. It then works through the process's external tasks as case events, the last of which finishes it.
        await().atMost(WAIT).untilAsserted(() -> assertThat(businessProcess(caseReference))
            .isEqualTo("MOVE_TO_DECISION_OUTCOME FINISHED"));
        assertThat(caseState(caseReference)).isEqualTo("DECISION_OUTCOME");
        assertThat(caseEvents(caseReference)).containsSubsequence(
            "MOVE_TO_DECISION_OUTCOME", "START_BUSINESS_PROCESS", "END_BUSINESS_PROCESS");
    }

    @Test
    void takingACaseOfflineCancelsItsTasks() {
        String caseReference = createCaseWithReviewTask();

        coreCaseDataService.triggerEvent(Long.parseLong(caseReference), CASE_PROCEEDS_IN_CASEMAN, Map.of(
            "claimProceedsInCaseman", Map.of("date", LocalDate.now().toString(), "reason", "APPLICATION")));

        assertThat(caseState(caseReference)).isEqualTo("PROCEEDS_IN_HERITAGE_SYSTEM");
        // The event also starts its business process, which stops at notifying RPA: the robotics record needs a whole
        // claim. Work allocation acts on the published event itself, so the cancellation does not depend on it.
        // Civil's cancellation DMN cancels the case progression tasks when a case proceeds in Caseman, which removes
        // them from Camunda. wa-task-monitor's termination job then terminates them in task management.
        await().atMost(WAIT).untilAsserted(() -> {
            cftlib().runWaTaskMonitorJob("TERMINATION");
            assertThat(tasks(caseReference)).containsExactly("reviewHearingException TERMINATED deleted");
        });
    }

    @Test
    void listingAHearingReadsItBackFromHmcAndReconfiguresTheCasesTasks() {
        String caseReference = createCaseWithReviewTask();
        String hearingId = requestHearing(caseReference);
        // The listing has to wait until HMI has acknowledged the request.
        await().atMost(WAIT).until(() -> hearingStatus(hearingId).equals("AWAITING_LISTING"));

        cftlib().sendHmiListing(hearingId, listing());

        // HMC publishes LISTED, this service triggers UPDATE_NEXT_HEARING_DETAILS, and its callback reads the case's
        // hearings back from HMC to find the next one.
        await().atMost(WAIT).untilAsserted(() -> assertThat(nextHearing(caseReference))
            .isEqualTo(hearingId + " " + LISTED_START.toLocalDate()));
        // Civil's cancellation DMN reconfigures the case's tasks on UPDATE_NEXT_HEARING_DETAILS. Case event handler
        // marks them, and wa-task-monitor's reconfiguration job reconfigures them from the case.
        await().atMost(WAIT).untilAsserted(() -> {
            cftlib().runWaTaskMonitorJob("RECONFIGURATION");
            assertThat(reconfiguredTasks(caseReference)).containsExactly("reviewHearingException");
        });
    }

    /**
     * A case in judicial referral with the configured task work allocation creates for a hearing HMI rejects.
     */
    private String createCaseWithReviewTask() {
        // A state work allocation's system role can read.
        String caseReference = createCase("JUDICIAL_REFERRAL");
        cftlib().rejectNextHmiRequest(2000, "cftlib could not list the hearing");
        requestHearing(caseReference);
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(caseEvents(caseReference)).contains("REVIEW_HEARING_EXCEPTION"));
        // Task management configures the task from the case, which WA's system user reads with civil's roles.
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(tasks(caseReference)).containsExactly("reviewHearingException UNASSIGNED null"));
        return caseReference;
    }

    /**
     * A CIVIL case holding only the fields these events and task configuration read: the parties, from whom the
     * task's case name comes, and the case management location, which gives its region and location. Creating a
     * claim through CCD would need the whole claim journey.
     */
    @SneakyThrows
    private String createCase(String state) {
        String data = """
            {
              "applicant1": {"type": "INDIVIDUAL", "individualFirstName": "Joe", "individualLastName": "Bloggs",
                             "partyName": "Joe Bloggs"},
              "respondent1": {"type": "INDIVIDUAL", "individualFirstName": "Jane", "individualLastName": "Doe",
                              "partyName": "Jane Doe"},
              "caseManagementLocation": {"region": "2", "baseLocation": "20262"}
            }
            """;
        String classification = """
            {
              "applicant1": {"classification": "PUBLIC", "value": {"type": "PUBLIC", "individualFirstName": "PUBLIC",
                             "individualLastName": "PUBLIC", "partyName": "PUBLIC"}},
              "respondent1": {"classification": "PUBLIC", "value": {"type": "PUBLIC", "individualFirstName": "PUBLIC",
                              "individualLastName": "PUBLIC", "partyName": "PUBLIC"}},
              "caseManagementLocation": {"classification": "PUBLIC",
                                         "value": {"region": "PUBLIC", "baseLocation": "PUBLIC"}}
            }
            """;
        String reference = caseReference();
        try (var connection = cftlib().getConnection(Database.Datastore)) {
            var insert = connection.prepareStatement(
                "insert into case_data (reference, jurisdiction, case_type_id, state, data, data_classification,"
                    + " security_classification, created_date, last_modified, last_state_modified_date)"
                    + " values (?, 'CIVIL', 'CIVIL', ?, ?::jsonb, ?::jsonb, 'PUBLIC', now(), now(), now())");
            insert.setLong(1, Long.parseLong(reference));
            insert.setString(2, state);
            insert.setString(3, data);
            insert.setString(4, classification);
            insert.execute();
        }
        return reference;
    }

    // CCD's case references are 16 digits with a Luhn check digit.
    private static String caseReference() {
        var digits = new StringBuilder("1");
        while (digits.length() < 15) {
            digits.append(ThreadLocalRandom.current().nextInt(10));
        }
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int digit = digits.charAt(14 - i) - '0';
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }
        return digits.append((10 - sum % 10) % 10).toString();
    }

    @SneakyThrows
    private String requestHearing(String caseReference) {
        String request = """
            {
              "requestDetails": {"requestTimeStamp": "%s"},
              "hearingDetails": {
                "hearingChannels": ["INTER"],
                "autolistFlag": false,
                "hearingType": "AAA7-TRI",
                "isAPanelFlag": false,
                "hearingWindow": {"dateRangeStart": "%s", "dateRangeEnd": "%s"},
                "duration": 60,
                "hearingPriorityType": "Standard",
                "hearingLocations": [{"locationType": "court", "locationId": "20262"}],
                "panelRequirements": {}
              },
              "caseDetails": {
                "hmctsServiceCode": "%s",
                "caseRef": "%s",
                "requestTimeStamp": "%s",
                "caseDeepLink": "http://localhost:3000/cases/case-details/%s",
                "hmctsInternalCaseName": "cftlib v cftlib",
                "publicCaseName": "cftlib v cftlib",
                "caseCategories": [
                  {"categoryType": "caseType", "categoryValue": "AAA7-FAST_CLAIM"},
                  {"categoryType": "caseSubType", "categoryValue": "AAA7-FAST_CLAIM", "categoryParent": "AAA7-FAST_CLAIM"}
                ],
                "caseManagementLocationCode": "20262",
                "caserestrictedFlag": false,
                "caseSLAStartDate": "%s"
              },
              "partyDetails": [{
                "partyID": "P1",
                "partyType": "IND",
                "partyRole": "CLAI",
                "individualDetails": {"title": "Mr", "firstName": "Joe", "lastName": "Bloggs"}
              }]
            }
            """.formatted(LocalDateTime.now(), LocalDate.now().plusDays(7), LocalDate.now().plusDays(60), SERVICE_CODE,
                          caseReference, LocalDateTime.now(), caseReference, LocalDate.now());
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(HEARING_SERVICE + "/hearing"))
                .header("Content-Type", "application/json")
                .header("Authorization", systemUpdate)
                .header("ServiceAuthorization", cftlib().generateDummyS2SToken("civil_service"))
                .POST(HttpRequest.BodyPublishers.ofString(request))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(201);
        return mapper.readTree(response.body()).path("hearingRequestID").asText();
    }

    @SneakyThrows
    private String hearingStatus(String hearingId) {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(HEARING_SERVICE + "/hearing/" + hearingId))
                .header("Authorization", systemUpdate)
                .header("ServiceAuthorization", cftlib().generateDummyS2SToken("civil_service"))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body()).path("requestDetails").path("status").asText();
    }

    private static String listing() {
        return """
            {
              "hearingResponse": {
                "meta": {"timestamp": "%s", "transactionIdCaseHQ": "cftlib-listing"},
                "hearing": {
                  "listingRequestId": "cftlib-listing",
                  "hearingCaseVersionId": 1,
                  "hearingCaseIdHMCTS": "cftlib-listing",
                  "hearingCaseStatus": {"code": "100", "description": "Listed"},
                  "hearingStatus": {"code": "FIXED", "description": "Fixed"},
                  "hearingIdCaseHQ": "cftlib-listing",
                  "hearingStartTime": "%s",
                  "hearingEndTime": "%s",
                  "hearingPrivate": false,
                  "hearingRisk": false,
                  "hearingTranslatorRequired": false,
                  "hearingCreatedDate": "%s",
                  "hearingCreatedBy": "cftlib",
                  "hearingVenue": {
                    "locationIdCaseHQ": "20262",
                    "locationName": "cftlib court",
                    "locationReferences": [{"key": "EPIMS", "value": "20262"}]
                  },
                  "hearingRoom": {"locationIdCaseHQ": "1", "locationName": "Room 1"}
                }
              }
            }
            """.formatted(LocalDateTime.now(), LISTED_START, LISTED_START.plusHours(1), LocalDateTime.now());
    }

    private List<String> caseEvents(String caseReference) {
        return query(Database.Datastore,
                     "select e.event_id from case_event e join case_data c on c.id = e.case_data_id"
                         + " where c.reference = ? order by e.id", Long.parseLong(caseReference));
    }

    private String caseState(String caseReference) {
        return query(Database.Datastore, "select state from case_data where reference = ?",
                     Long.parseLong(caseReference)).get(0);
    }

    private String businessProcess(String caseReference) {
        return query(Database.Datastore,
                     "select concat(data->'businessProcess'->>'camundaEvent', ' ', data->'businessProcess'->>'status')"
                         + " from case_data where reference = ?", Long.parseLong(caseReference)).get(0);
    }

    // The next hearing's id and date, as this service records them.
    private String nextHearing(String caseReference) {
        return query(Database.Datastore,
                     "select concat(data->'nextHearingDetails'->>'hearingID', ' ',"
                         + " left(data->'nextHearingDetails'->>'hearingDateTime', 10)) from case_data where reference = ?",
                     Long.parseLong(caseReference)).get(0);
    }

    // Each of the case's tasks in task management, as its type, state and termination reason.
    private List<String> tasks(String caseReference) {
        return taskQuery("select concat(task_type, ' ', state, ' ', coalesce(termination_reason, 'null'))"
                             + " from cft_task_db.tasks where case_id = ?", caseReference);
    }

    private List<String> reconfiguredTasks(String caseReference) {
        return taskQuery("select task_type from cft_task_db.tasks where case_id = ?"
                             + " and last_reconfiguration_time is not null", caseReference);
    }

    @SneakyThrows
    private List<String> taskQuery(String sql, String caseReference) {
        try (var connection = DriverManager.getConnection("jdbc:postgresql://localhost:6432/cft_task_db",
                                                          "postgres", "postgres")) {
            return rows(connection.prepareStatement(sql), caseReference);
        }
    }

    @SneakyThrows
    private List<String> query(Database database, String sql, Object parameter) {
        try (var connection = cftlib().getConnection(database)) {
            return rows(connection.prepareStatement(sql), parameter);
        }
    }

    @SneakyThrows
    private static List<String> rows(java.sql.PreparedStatement statement, Object parameter) {
        statement.setObject(1, parameter);
        List<String> rows = new ArrayList<>();
        var results = statement.executeQuery();
        while (results.next()) {
            rows.add(results.getString(1));
        }
        return rows;
    }
}
