package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CcdModelCompatibilityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldDeserializeAddCaseAssignedUserRolesResponseWithStatusMessage() throws Exception {
        String payload = """
            {
              "status": "CREATED",
              "status_message": "role assigned",
              "unexpected_field": "ignored"
            }
            """;

        AddCaseAssignedUserRolesResponse response =
            OBJECT_MAPPER.readValue(payload, AddCaseAssignedUserRolesResponse.class);

        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getStatusMessage()).isEqualTo("role assigned");
    }

    @Test
    void shouldIgnoreUnknownOrganisationPolicyFields() throws Exception {
        String payload = """
            {
              "Organisation": {
                "OrganisationID": "ORG-1"
              },
              "OrgPolicyReference": "POL-123",
              "OrgPolicyCaseAssignedRole": "[APPLICANTSOLICITORONE]",
              "PrepopulateToUsersOrganisation": true
            }
            """;

        OrganisationPolicy policy = OBJECT_MAPPER.readValue(payload, OrganisationPolicy.class);

        assertThat(policy.getOrganisation()).isNotNull();
        assertThat(policy.getOrganisation().getOrganisationID()).isEqualTo("ORG-1");
        assertThat(policy.getOrgPolicyReference()).isEqualTo("POL-123");
        assertThat(policy.getOrgPolicyCaseAssignedRole()).isEqualTo("[APPLICANTSOLICITORONE]");
    }
}
