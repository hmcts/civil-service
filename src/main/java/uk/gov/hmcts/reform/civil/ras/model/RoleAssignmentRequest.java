package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@Jacksonized
@AllArgsConstructor
public class RoleAssignmentRequest {

    private List<RoleAssignment> requestedRoles;
    private RoleRequest roleRequest;

}
