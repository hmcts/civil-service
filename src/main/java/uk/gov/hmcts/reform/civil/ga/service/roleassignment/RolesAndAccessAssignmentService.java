package uk.gov.hmcts.reform.civil.ga.service.roleassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolesAndAccessAssignmentService {

    public static final List<String> ROLE_TYPE = List.of("CASE");
    public static final List<String> ROLE_NAMES = List.of("allocated-judge", "lead-judge", "allocated-legal-adviser",
        "allocated-admin-caseworker", "allocated-ctsc-caseworker", "allocated-nbc-caseworker");
    private final RoleAssignmentsService roleAssignmentService;
    private final UserService userService;
    private final SystemUpdateUserConfiguration systemUserConfig;

    public void copyAllocatedRolesFromRolesAndAccess(String mainCaseId, String gaCaseId) {
        try {
            getCaseRoles(mainCaseId, gaCaseId, getSystemUserToken());
        } catch (Exception e) {
            log.error("Could not automatically copy and assign roles from Roles And Access", e);
        }
    }

    private void getCaseRoles(String mainCaseId, String gaCaseId, String bearerToken) {
        RoleAssignmentServiceResponse roleAssignmentResponse = roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(mainCaseId, ROLE_TYPE, ROLE_NAMES, bearerToken);
        Optional.ofNullable(roleAssignmentResponse.getRoleAssignmentResponse())
            .ifPresentOrElse(response -> {
                if (response.isEmpty()) {
                    log.info("Role assignment list empty for case ID, and allocated roles {}", mainCaseId);
                } else {
                    assignRoles(gaCaseId, roleAssignmentResponse, bearerToken);
                }
            }, () -> log.info("No role assignment response found for case ID {}", mainCaseId));
    }

    private void assignRoles(String gaCaseId, RoleAssignmentServiceResponse rolesToAssign, String bearerToken) {
        String systemUserId = userService.getUserInfo(bearerToken).getUid();

        // get current GA role assignments
        RoleAssignmentServiceResponse existingGaRoleAssignments = roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(
            gaCaseId, ROLE_TYPE, ROLE_NAMES, bearerToken
        );

        roleAssignmentService.assignUserRoles(
            systemUserId,
            bearerToken,
            RoleAssignmentRequest.builder()
                .roleRequest(RoleRequest.builder()
                                 .assignerId(systemUserId)
                                 .replaceExisting(false)
                                 .build())
                .requestedRoles(buildRoleAssignments(gaCaseId, rolesToAssign, existingGaRoleAssignments)).build());
        log.info("Assigned roles from main case, to GA successfully");
    }

    private static List<RoleAssignment> buildRoleAssignments(String gaCaseId, RoleAssignmentServiceResponse roleToAssign,
                                                             RoleAssignmentServiceResponse existingGaRoleAssignments) {
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        Map<String, List<RoleAssignmentResponse>> roleAssignmentsByActorId = roleToAssign.getRoleAssignmentResponse().stream()
            .collect(Collectors.groupingBy(RoleAssignmentResponse::getActorId));

        roleAssignmentsByActorId.forEach((actorId, roleResponses) -> {
            roleResponses.forEach(roleResponse -> {

                // Check if an assignment has already been copied
                boolean assignmentCopyExists = Optional.ofNullable(existingGaRoleAssignments.getRoleAssignmentResponse())
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(ra -> ra.getActorId().equals(roleResponse.getActorId())
                        && ra.getRoleName().equals(roleResponse.getRoleName()));

                if (!assignmentCopyExists) {
                    Map<String, Object> roleAssignmentAttributes = new HashMap<>();
                    roleAssignmentAttributes.put("caseId", gaCaseId);
                    roleAssignmentAttributes.put("caseType", "GENERALAPPLICATION");
                    roleAssignmentAttributes.put("jurisdiction", "CIVIL");

                    roleAssignments.add(RoleAssignment.builder()
                                            .actorId(roleResponse.getActorId())
                                            .actorIdType(roleResponse.getActorIdType())
                                            .grantType(GrantType.valueOf(roleResponse.getGrantType()))
                                            .roleCategory(RoleCategory.valueOf(roleResponse.getRoleCategory()))
                                            .roleType(RoleType.valueOf(roleResponse.getRoleType()))
                                            .classification(roleResponse.getClassification())
                                            .roleName(roleResponse.getRoleName())
                                            .beginTime(roleResponse.getBeginTime())
                                            .endTime(roleResponse.getEndTime())
                                            .readOnly(false)
                                            .attributes(roleAssignmentAttributes)
                                            .build());
                }
            });
        });
        return roleAssignments;
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(systemUserConfig.getUserName(), systemUserConfig.getPassword());
    }

}
