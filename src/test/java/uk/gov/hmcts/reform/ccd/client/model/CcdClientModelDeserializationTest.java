package uk.gov.hmcts.reform.ccd.client.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CcdClientModelDeserializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldDeserializeCallbackRequestPayload() throws Exception {
        String payload = """
            {
              "event_id": "CREATE_CLAIM",
              "case_details": {
                "id": 12345,
                "case_type_id": "CIVIL",
                "state": "CASE_ISSUED",
                "case_data": {
                  "detailsOfClaim": "details"
                }
              },
              "case_details_before": {
                "id": 12344,
                "case_type_id": "CIVIL",
                "state": "DRAFT",
                "case_data": {
                  "detailsOfClaim": "before"
                }
              }
            }
            """;

        CallbackRequest callbackRequest = OBJECT_MAPPER.readValue(payload, CallbackRequest.class);

        assertThat(callbackRequest.getEventId()).isEqualTo("CREATE_CLAIM");
        assertThat(callbackRequest.getCaseDetails()).isNotNull();
        assertThat(callbackRequest.getCaseDetails().getId()).isEqualTo(12345L);
        assertThat(callbackRequest.getCaseDetails().getCaseTypeId()).isEqualTo("CIVIL");
        assertThat(callbackRequest.getCaseDetails().getData()).containsEntry("detailsOfClaim", "details");
        assertThat(callbackRequest.getCaseDetailsBefore()).isNotNull();
        assertThat(callbackRequest.getCaseDetailsBefore().getId()).isEqualTo(12344L);
    }

    @Test
    void shouldDeserializeCaseDetailsPayload() throws Exception {
        String payload = """
            {
              "id": 12345,
              "jurisdiction": "CIVIL",
              "case_type_id": "CIVIL",
              "created_date": "2025-10-23T00:00:00",
              "last_modified": "2025-10-24T00:00:00",
              "state": "CASE_ISSUED",
              "locked_by_user_id": 100,
              "security_level": 2,
              "case_data": {
                "detailsOfClaim": "details"
              },
              "callback_response_status": "SUBMITTED",
              "version": 9
            }
            """;

        CaseDetails caseDetails = OBJECT_MAPPER.readValue(payload, CaseDetails.class);

        assertThat(caseDetails.getId()).isEqualTo(12345L);
        assertThat(caseDetails.getCaseTypeId()).isEqualTo("CIVIL");
        assertThat(caseDetails.getState()).isEqualTo("CASE_ISSUED");
        assertThat(caseDetails.getLockedBy()).isEqualTo(100);
        assertThat(caseDetails.getSecurityLevel()).isEqualTo(2);
        assertThat(caseDetails.getData()).containsEntry("detailsOfClaim", "details");
        assertThat(caseDetails.getCallbackResponseStatus()).isEqualTo("SUBMITTED");
        assertThat(caseDetails.getVersion()).isEqualTo(9);
    }

    @Test
    void shouldDeserializeCaseAssignmentUserRolePayload() throws Exception {
        String payload = """
            {
              "case_id": "12345",
              "user_id": "user-1",
              "case_role": "[APPLICANTSOLICITORONE]"
            }
            """;

        CaseAssignmentUserRole caseAssignmentUserRole = OBJECT_MAPPER.readValue(payload, CaseAssignmentUserRole.class);

        assertThat(caseAssignmentUserRole.getCaseDataId()).isEqualTo("12345");
        assertThat(caseAssignmentUserRole.getUserId()).isEqualTo("user-1");
        assertThat(caseAssignmentUserRole.getCaseRole()).isEqualTo("[APPLICANTSOLICITORONE]");
    }
}
