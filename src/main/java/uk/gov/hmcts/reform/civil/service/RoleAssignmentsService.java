package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.ras.client.RoleAssignmentsApi;
import uk.gov.hmcts.reform.civil.ras.model.QueryRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAssignmentsService {

    private final RoleAssignmentsApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;

    public RoleAssignmentServiceResponse getRoleAssignments(String actorId,
                                                            String authorization) {

        if (log.isDebugEnabled()) {
            log.debug(actorId, "Getting Role assignments for actorId {0}");
        }

        return roleAssignmentApi.getRoleAssignments(
            authorization,
            authTokenGenerator.generate(),
            actorId
        );
    }

    public RoleAssignmentServiceResponse getRoleAssignmentsWithLabels(String actorId, String authorization, List<String> roleNames) {

        if (log.isDebugEnabled()) {
            log.debug(actorId, "Getting Role assignments for actorId {0}");
        }

        return roleAssignmentApi.getRoleAssignments(
            authorization,
            authTokenGenerator.generate(),
            null,
            null,
            100,
            null,
            null,
            QueryRequest.builder()
                .actorId(actorId)
                .roleName(roleNames)
                .build(),
            true
        );
    }

    public RoleAssignmentServiceResponse queryRoleAssignmentsByCaseIdAndRole(String caseId, List<String> roleType,
                                                              List<String> roleName, String authorization) {
        if (log.isDebugEnabled()) {
            log.debug(caseId, "Getting Role assignments for case ID {0}");
        }

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(roleType)
            .roleName(roleName)
            .attributes(Map.of("caseId", List.of(caseId)))
            .build();

        return this.roleAssignmentApi.getRoleAssignments(authorization,
                                                         this.authTokenGenerator.generate(),
                                                         null,
                                                         null,
                                                         null,
                                                         "roleName",
                                                         null,
                                                         queryRequest,
                                                         true);
    }

    public void assignUserRoles(String actorId, String authorization, RoleAssignmentRequest roleAssignmentRequest) {
        if (log.isDebugEnabled()) {
            log.debug(actorId, "Assigning roles to actorId {0}");
        }

        roleAssignmentApi.createRoleAssignment(authorization, authTokenGenerator.generate(), roleAssignmentRequest);
    }
}
