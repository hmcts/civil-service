package uk.gov.hmcts.reform.civil.service.roleassignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.ras.model.GrantType;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignment;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleCategory;
import uk.gov.hmcts.reform.civil.ras.model.RoleRequest;
import uk.gov.hmcts.reform.civil.ras.model.RoleType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.UserService;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "role-initialisation.enabled", havingValue = "true")
public class RoleAssignmentInitialisationService {

    private static final String HEARINGS_SYSTEM_USER_REFERENCE = "civil-hearings-system-user";
    private static final String CASE_ALLOCATOR_SYSTEM_USER_REFERENCE = "civil-case-allocator-system-user";
    private static final String SYSTEM_USER_PROCESS = "civil-system-user";

    private final SystemUpdateUserConfiguration systemUserConfig;
    private final RoleAssignmentsService roleAssignmentService;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;

    @PostConstruct
    public void initialiseUserRolesOnStartUp() {
        try {
            assignHearingRoles(getSystemUserToken());
        } catch (Exception e) {
            log.error("Could not automatically create user role assignment", e);
        }

        try {
            assignCaseAllocatorToSystemUser(getSystemUserToken());
        } catch (Exception e) {
            log.error("Could not automatically create case allocator role assignment", e);
        }
    }

    private void assignHearingRoles(String userAuth) {
        log.info("Attempting to assign hearing roles");
        String userId = userService.getUserInfo(userAuth).getUid();
        RoleRequest roleRequest = new RoleRequest()
            .setAssignerId(userId)
            .setReference(HEARINGS_SYSTEM_USER_REFERENCE)
            .setProcess(SYSTEM_USER_PROCESS)
            .setReplaceExisting(true);

        RoleAssignmentRequest request = new RoleAssignmentRequest()
            .setRoleRequest(roleRequest)
            .setRequestedRoles(createCivilSystemRoles(userId, "hearing-manager", "hearing-viewer"));

        roleAssignmentService.assignUserRoles(userId, userAuth, request);
        log.info("Assigned roles successfully");
    }

    private void assignCaseAllocatorToSystemUser(String userAuth) {
        log.info("Attempting to assign case allocator to system user");
        String userId = userService.getUserInfo(userAuth).getUid();
        RoleRequest roleRequest = new RoleRequest()
            .setAssignerId(userId)
            .setReference(CASE_ALLOCATOR_SYSTEM_USER_REFERENCE)
            .setProcess(SYSTEM_USER_PROCESS)
            .setReplaceExisting(true);

        RoleAssignmentRequest request = new RoleAssignmentRequest()
            .setRoleRequest(roleRequest)
            .setRequestedRoles(createAllocatedSystemRoles(userId, "CIVIL", "GENERALAPPLICATION"));

        roleAssignmentService.assignUserRoles(userId, userAuth, request);
        log.info("Assigned case allocator roles successfully");
    }

    private List<RoleAssignment> createCivilSystemRoles(String userId, String... roleNames) {
        return Arrays.asList(roleNames).stream().map(roleName -> new RoleAssignment()
            .setActorId(userId)
            .setActorIdType("IDAM")
            .setRoleType(RoleType.ORGANISATION)
            .setClassification("PUBLIC")
            .setGrantType(GrantType.STANDARD)
            .setRoleCategory(RoleCategory.SYSTEM)
            .setRoleName(roleName)
            .setAttributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
            .setReadOnly(false)).toList();
    }

    private List<RoleAssignment> createAllocatedSystemRoles(String userId, String... caseTypes) {
        return Arrays.asList(caseTypes).stream().map(caseType -> new RoleAssignment()
            .setActorId(userId)
            .setActorIdType("IDAM")
            .setRoleType(RoleType.ORGANISATION)
            .setClassification("PUBLIC")
            .setGrantType(GrantType.STANDARD)
            .setRoleCategory(RoleCategory.SYSTEM)
            .setRoleName("case-allocator")
            .setAttributes(Map.of("jurisdiction", "CIVIL", "caseType", caseType))
            .setReadOnly(false)).toList();
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(systemUserConfig.getUserName(), systemUserConfig.getPassword());
    }
}
