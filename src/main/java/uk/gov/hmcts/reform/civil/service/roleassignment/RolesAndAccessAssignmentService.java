package uk.gov.hmcts.reform.civil.service.roleassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ras.model.GrantType;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignment;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleCategory;
import uk.gov.hmcts.reform.civil.ras.model.RoleRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleType;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RolesAndAccessAssignmentService {

    public static final List<String> ROLE_TYPE = List.of("CASE");
    public static final List<String> ROLE_NAMES = List.of("allocated-judge", "lead-judge", "allocated-legal-adviser",
        "allocated-admin-caseworker", "allocated-ctsc-caseworker", "allocated-nbc-caseworker");
    private final RoleAssignmentsService roleAssignmentService;
    private final UserService userService;

    public void copyAllocatedRolesFromRolesAndAccess(String caseId, String bearerToken) {
        try {
            getCaseRoles(caseId, bearerToken);
        } catch (Exception e) {
            log.error("Could not automatically copy and assign roles from Roles And Access", e);
        }
    }

    private void getCaseRoles(String caseId, String bearerToken) {
        RoleAssignmentServiceResponse roleAssignmentResponse = roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(caseId, ROLE_TYPE, ROLE_NAMES, bearerToken);
        Optional.ofNullable(roleAssignmentResponse.getRoleAssignmentResponse())
            .ifPresentOrElse(response -> {
                log.info("GET ROLES case id roleAssignmentResponse:  {}", response);
                Map<String, List<RoleAssignmentResponse>> roleAssignmentsByActorId = response.stream()
                    .collect(Collectors.groupingBy(RoleAssignmentResponse::getActorId));

                roleAssignmentsByActorId.forEach((actorId, actorRoles)
                    -> actorRoles.forEach(role -> assignRoles(caseId, role)));
            }, () -> log.info("No role assignment response found for case ID {}", caseId));
    }

    private void assignRoles(String caseId, RoleAssignmentResponse roleToAssign) {
        //TODO we will use system user to make assignment, currently unavailable, so temporary using hardcoded ID
        String userAuth = getSystemUserToken();
        String systemUserId = userService.getUserInfo(userAuth).getUid();

        roleAssignmentService.assignUserRoles(
            systemUserId,
            userAuth,
            RoleAssignmentRequest.builder()
                .roleRequest(RoleRequest.builder()
                                 .assignerId(systemUserId)
                                 .replaceExisting(false)
                                 .build())
                .requestedRoles(buildRoleAssignments(caseId, Collections.singletonList(roleToAssign.getActorId()), roleToAssign)).build());
        log.info("Assigned roles successfully");
    }

    private static RoleAssignment buildRoleAssignment(String caseId, String userId, RoleAssignmentResponse roleToAssign) {
        return RoleAssignment.builder()
            .actorId(userId)
            .actorIdType(roleToAssign.getActorIdType())
            .grantType(GrantType.valueOf(roleToAssign.getGrantType()))
            .roleCategory(RoleCategory.valueOf(roleToAssign.getRoleCategory()))
            .roleType(RoleType.valueOf(roleToAssign.getRoleType()))
            .classification(roleToAssign.getClassification())
            .roleName(roleToAssign.getRoleName())
            .beginTime(roleToAssign.getBeginTime())
            .endTime(roleToAssign.getEndTime())
            .readOnly(false)
            .attributes(Map.of("caseId", "1737393393405565",
                               "caseType", "CIVIL",
                               "jurisdiction", "CIVIL",
                               "contractType", roleToAssign.getAttributes().getContractType(),
                               "region", roleToAssign.getAttributes().getRegion(),
                               "primaryLocation", roleToAssign.getAttributes().getPrimaryLocation()
            )).build();
    }

    private static List<RoleAssignment> buildRoleAssignments(String caseId, List<String> userIds, RoleAssignmentResponse roleToAssign) {
        return userIds.stream()
            .map(user -> buildRoleAssignment(caseId, user, roleToAssign))
            .collect(Collectors.toList());
    }

    private String getSystemUserToken() {
        return userService.getAccessToken("4917924EMP-@ejudiciary.net", "Hmcts1234");
    }

}
