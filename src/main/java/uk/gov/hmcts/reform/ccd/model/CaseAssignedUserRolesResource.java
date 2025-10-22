package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class CaseAssignedUserRolesResource {

    @JsonProperty("case_users")
    private List<CaseAssignedUserRole> caseAssignedUserRoles;
}
