package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class AddCaseAssignedUserRolesResponse {

    private String status;
}
