package uk.gov.hmcts.reform.civil.cftlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.rse.ccd.lib.Database;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Hearings and work allocation with cftlib's hmc and wa features. A hearing's updates reach this service on the
 * hmc-to-cft topic, as in an environment, and the case events it triggers create work allocation tasks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CivilHearingsCftlibTest extends CftlibTest {

    private static final String HEARING_SERVICE = "http://localhost:4561";
    private static final String SYSTEM_UPDATE = "civil-system-update@mailnesia.com";
    // The specified claims service code, which this service's hmc-to-cft subscription and handler accept.
    private static final String SERVICE_CODE = "AAA7";
    private static final Duration WAIT = Duration.ofMinutes(2);

    @Autowired
    private UserService userService;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String systemUpdate;

    @BeforeAll
    void signIn() {
        systemUpdate = userService.getAccessToken(SYSTEM_UPDATE, "password");
    }

    @Test
    void reviewsAHearingThatHmiCannotList() {
        // Task configuration takes the task's case name, region and location from the case.
        String caseReference = createCase(
            "{\"applicant1\": {\"partyName\": \"cftlib\"},"
                + " \"caseManagementLocation\": {\"region\": \"2\", \"baseLocation\": \"20262\"}}",
            "{\"applicant1\": {\"classification\": \"PUBLIC\", \"value\": {\"partyName\": \"PUBLIC\"}},"
                + " \"caseManagementLocation\": {\"classification\": \"PUBLIC\","
                + " \"value\": {\"region\": \"PUBLIC\", \"baseLocation\": \"PUBLIC\"}}}");
        cftlib().rejectNextHmiRequest(2000, "cftlib could not list the hearing");

        requestHearing(caseReference);

        // HMC puts the hearing in EXCEPTION, which this service turns into REVIEW_HEARING_EXCEPTION, whose initiation
        // DMN creates a task.
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(caseEvents(caseReference)).contains("REVIEW_HEARING_EXCEPTION"));
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(taskNames(caseReference)).contains("Review HMC Hearing Request Failure"));
        // Task management configures the task from the case, which WA's system user reads with civil's roles.
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(configuredTasks(caseReference)).containsExactly("reviewHearingException UNASSIGNED 20262"));
    }

    @Test
    void updatesTheNextHearingWhenAHearingIsListed() {
        String caseReference = createCase();
        String hearingId = requestHearing(caseReference);
        // The listing has to wait until HMI has acknowledged the request.
        await().atMost(WAIT).until(() -> hearingStatus(hearingId).equals("AWAITING_LISTING"));

        cftlib().sendHmiListing(hearingId, listing());

        // UPDATE_NEXT_HEARING_DETAILS's callback reads the listed hearing back from HMC.
        await().atMost(WAIT).untilAsserted(
            () -> assertThat(caseEvents(caseReference)).contains("UPDATE_NEXT_HEARING_DETAILS"));
    }

    /**
     * A CIVIL case with no claim in it, as these events need nothing more. Creating a claim through CCD would need
     * the whole claim journey. It is in a state that work allocation's system role can read.
     */
    private String createCase() {
        return createCase("{}", "{}");
    }

    @SneakyThrows
    private String createCase(String data, String classification) {
        String reference = caseReference();
        try (var connection = cftlib().getConnection(Database.Datastore)) {
            var insert = connection.prepareStatement(
                "insert into case_data (reference, jurisdiction, case_type_id, state, data, data_classification,"
                    + " security_classification, created_date, last_modified, last_state_modified_date)"
                    + " values (?, 'CIVIL', 'CIVIL', 'JUDICIAL_REFERRAL', ?::jsonb, ?::jsonb, 'PUBLIC',"
                    + " now(), now(), now())");
            insert.setLong(1, Long.parseLong(reference));
            insert.setString(2, data);
            insert.setString(3, classification);
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
        LocalDateTime start = LocalDate.now().plusDays(14).atTime(10, 0);
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
            """.formatted(LocalDateTime.now(), start, start.plusHours(1), LocalDateTime.now());
    }

    @SneakyThrows
    private List<String> caseEvents(String caseReference) {
        List<String> events = new ArrayList<>();
        try (var connection = cftlib().getConnection(Database.Datastore)) {
            var query = connection.prepareStatement(
                "select e.event_id from case_event e join case_data c on c.id = e.case_data_id"
                    + " where c.reference = ? order by e.id");
            query.setLong(1, Long.parseLong(caseReference));
            var rows = query.executeQuery();
            while (rows.next()) {
                events.add(rows.getString(1));
            }
        }
        return events;
    }

    // Each of the case's tasks in task management, as its type, state and location.
    @SneakyThrows
    private List<String> configuredTasks(String caseReference) {
        List<String> tasks = new ArrayList<>();
        try (var connection = DriverManager.getConnection("jdbc:postgresql://localhost:6432/cft_task_db",
                                                          "postgres", "postgres")) {
            var query = connection.prepareStatement(
                "select task_type, state, location from cft_task_db.tasks where case_id = ?");
            query.setString(1, caseReference);
            var rows = query.executeQuery();
            while (rows.next()) {
                tasks.add(rows.getString(1) + " " + rows.getString(2) + " " + rows.getString(3));
            }
        }
        return tasks;
    }

    @SneakyThrows
    private List<String> taskNames(String caseReference) {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(CivilCftlibConfig.CAMUNDA_URL + "/engine-rest/task?processVariables="
                                                  + URLEncoder.encode("caseId_eq_" + caseReference,
                                                                      StandardCharsets.UTF_8)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        List<String> names = new ArrayList<>();
        for (JsonNode task : mapper.readTree(response.body())) {
            names.add(task.path("name").asText());
        }
        return names;
    }
}
