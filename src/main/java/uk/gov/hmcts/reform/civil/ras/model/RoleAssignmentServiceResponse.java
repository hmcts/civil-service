package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RoleAssignmentServiceResponse {

    private List<RoleAssignmentResponse> roleAssignmentResponse;

}

