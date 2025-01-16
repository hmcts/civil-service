package uk.gov.hmcts.reform.civil.service.roleassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.ras.model.GrantType;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignment;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleCategory;
import uk.gov.hmcts.reform.civil.ras.model.RoleRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleType;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudgeRoleAssignmentInitialisationService {

    private final SystemUpdateUserConfiguration systemUserConfig;
    private final RoleAssignmentsService roleAssignmentService;
    private final UserService userService;

    public void assignJudgeRoles(String caseId, String userId, String judgeRole, ZonedDateTime beginTime, ZonedDateTime endTime) {
        List<String> userList = new ArrayList<>();
        String userAuth = getSystemUserToken();
        log.info("Attempting to assign judge roles");
        String systemUserId = userService.getUserInfo(userAuth).getUid();
        log.info("userAuth token {}", userAuth);
        log.info("userAuth id {}", systemUserId);
        userList.add(systemUserId);
        roleAssignmentService.assignUserRoles(
            systemUserId,
            userAuth,
            RoleAssignmentRequest.builder()
                .roleRequest(RoleRequest.builder()
                                 .assignerId(systemUserId)
                                 .replaceExisting(false)
                                 .build())
                .requestedRoles(buildRoleAssignments(caseId, userList, "allocated-judge")).build()
        );
        log.info("Assigned roles successfully");
    }

    public static RoleAssignment buildRoleAssignment(String caseId, String userId, String role) {
        return RoleAssignment.builder()
            .actorId(userId)
            .attributes(Map.of("caseId", caseId.toString(),
                               "caseType", "CIVIL",
                               "jurisdiction", "CIVIL",
                               "substantive", "Y"))
            .grantType(GrantType.SPECIFIC)
            .roleCategory(RoleCategory.JUDICIAL)
            .roleType(RoleType.CASE)
            .beginTime(ZonedDateTime.now())
            .endTime(ZonedDateTime.now().plusDays(1))
            .roleName(role)
            .readOnly(false)
            .build();
    }

    public static List<RoleAssignment> buildRoleAssignments(String caseId, List<String> userIds, String role) {
        return userIds.stream()
            .map(user -> buildRoleAssignment(caseId, user, role))
            .collect(Collectors.toList());
    }

    private String getSystemUserToken() {
        return userService.getAccessToken("4917924EMP-@ejudiciary.net", "Hmcts1234");
    }
}
