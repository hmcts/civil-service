package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {

    private List<RoleAssignment> requestedRoles;
    private RoleRequest roleRequest;

}
