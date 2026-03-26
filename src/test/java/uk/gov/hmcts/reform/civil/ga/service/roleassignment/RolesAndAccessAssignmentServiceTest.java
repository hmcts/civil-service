package uk.gov.hmcts.reform.civil.ga.service.roleassignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.ras.model.Attributes;
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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.ga.service.roleassignment.RolesAndAccessAssignmentService.ROLE_NAMES;
import static uk.gov.hmcts.reform.civil.ga.service.roleassignment.RolesAndAccessAssignmentService.ROLE_TYPE;

@ExtendWith(MockitoExtension.class)
class RolesAndAccessAssignmentServiceTest {

    @Mock
    private RoleAssignmentsService roleAssignmentService;
    @Mock
    private UserService userService;
    @Mock
    private SystemUpdateUserConfiguration systemUserConfig;
    @Mock
    RoleAssignmentServiceResponse roleAssignmentResponse;
    @InjectMocks
    private RolesAndAccessAssignmentService rolesAndAccessAssignmentService;

    private final String mainCaseId = "1234567890";
    private final String gaCaseId = "0987654321";
    private final String bearerToken = "bearerToken";
    private final String systemUserAuth = "system_user_id";
    private final String judgeUserToCopyInto = "example_user";
    private final String adminUserToCopyInto = "example_user2";
    private RoleAssignmentServiceResponse allocatedCaseRoles;
    private RoleAssignmentServiceResponse prexistingGAallocatedCaseRoles;

    @BeforeEach
    void setUp() {
        when(userService.getAccessToken(any(), any())).thenReturn(bearerToken);

        RoleAssignmentResponse allocatedJudgeRoleAssignment = new RoleAssignmentResponse()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setRoleType("CASE")
            .setRoleName("allocated-judge")
            .setRoleLabel("Allocated Judge")
            .setClassification("RESTRICTED")
            .setGrantType("SPECIFIC")
            .setRoleCategory("JUDICIAL")
            .setReadOnly(false)
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setCreated(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setAttributes(new Attributes()
                            .setSubstantive("Y")
                            .setCaseId(mainCaseId)
                            .setCaseType("CIVIL")
                            .setPrimaryLocation(null)
                            .setContractType(null)
                            .setRegion(null));
        RoleAssignmentResponse leadJudgeRoleAssignment = new RoleAssignmentResponse()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setRoleType("CASE")
            .setRoleName("lead-judge")
            .setRoleLabel("Lead Judge")
            .setClassification("RESTRICTED")
            .setGrantType("SPECIFIC")
            .setRoleCategory("JUDICIAL")
            .setReadOnly(false)
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setCreated(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setAttributes(new Attributes()
                            .setSubstantive("Y")
                            .setCaseId(mainCaseId)
                            .setCaseType("CIVIL")
                            .setPrimaryLocation(null)
                            .setContractType(null)
                            .setRegion(null));
        RoleAssignmentResponse adminRoleAssignment = new RoleAssignmentResponse()
            .setActorId(adminUserToCopyInto)
            .setActorIdType("IDAM")
            .setRoleType("CASE")
            .setRoleName("allocated-admin-caseworker")
            .setRoleLabel("allocated admin")
            .setClassification("RESTRICTED")
            .setGrantType("SPECIFIC")
            .setRoleCategory("ADMIN")
            .setReadOnly(false)
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setCreated(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setAttributes(new Attributes()
                            .setSubstantive("Y")
                            .setCaseId(mainCaseId)
                            .setCaseType("CIVIL")
                            .setRegion(null));

        List<RoleAssignmentResponse> roleList = new ArrayList<>();
        roleList.add(allocatedJudgeRoleAssignment);
        roleList.add(leadJudgeRoleAssignment);
        roleList.add(adminRoleAssignment);
        allocatedCaseRoles = new RoleAssignmentServiceResponse().setRoleAssignmentResponse(roleList);

        // pre-existing copies should not be re-copied
        RoleAssignmentResponse preExistingCopiedAdminRoleAssignment = new RoleAssignmentResponse()
            .setActorId(adminUserToCopyInto)
            .setActorIdType("IDAM")
            .setRoleType("CASE")
            .setRoleName("allocated-admin-caseworker")
            .setRoleLabel("allocated admin")
            .setClassification("RESTRICTED")
            .setGrantType("SPECIFIC")
            .setRoleCategory("ADMIN")
            .setReadOnly(false)
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setCreated(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setAttributes(new Attributes()
                            .setSubstantive("Y")
                            .setCaseId(gaCaseId)
                            .setCaseType("GENERALAPPLICATION")
                            .setRegion(null));

        List<RoleAssignmentResponse> existingGaRoleList = new ArrayList<>();
        existingGaRoleList.add(preExistingCopiedAdminRoleAssignment);
        prexistingGAallocatedCaseRoles = new RoleAssignmentServiceResponse().setRoleAssignmentResponse(existingGaRoleList);
    }

    @Test
    void shouldGetCaseRoles_whenInvoked() {
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(allocatedCaseRoles);

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        verify(roleAssignmentService, times(1)).queryRoleAssignmentsByCaseIdAndRole(
            eq(mainCaseId),
            eq(ROLE_TYPE),
            eq(ROLE_NAMES),
            eq(bearerToken));
    }

    @Test
    void shouldNotTryToAssignCaseRoles_whenInvokedAndListEmpty() {
        when(roleAssignmentResponse.getRoleAssignmentResponse()).thenReturn(new ArrayList<>());
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(roleAssignmentResponse);

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        verify(roleAssignmentService, never()).assignUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldCopyCaseRolesFromMainCaseIntoGaCase() {
        when(userService.getUserInfo(bearerToken)).thenReturn(UserInfo.builder().uid(systemUserAuth).build());
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(allocatedCaseRoles);
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(gaCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(new RoleAssignmentServiceResponse(Collections.emptyList()));

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        RoleRequest expectedRoleRequest = new RoleRequest()
            .setAssignerId(systemUserAuth)
            .setReplaceExisting(false);

        RoleAssignment allocatedJudge = new RoleAssignment()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setRoleType(RoleType.CASE)
            .setClassification("RESTRICTED")
            .setGrantType(GrantType.SPECIFIC)
            .setRoleCategory(RoleCategory.JUDICIAL)
            .setRoleName("allocated-judge")
            .setAttributes(Map.of("jurisdiction", "CIVIL",
                                   "caseType", "GENERALAPPLICATION",
                                   "caseId", gaCaseId))
            .setReadOnly(false);

        RoleAssignment leadJudge = new RoleAssignment()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setRoleType(RoleType.CASE)
            .setClassification("RESTRICTED")
            .setGrantType(GrantType.SPECIFIC)
            .setRoleCategory(RoleCategory.JUDICIAL)
            .setRoleName("lead-judge")
            .setAttributes(Map.of("jurisdiction", "CIVIL",
                                   "caseType", "GENERALAPPLICATION",
                                   "caseId", gaCaseId))
            .setReadOnly(false);

        RoleAssignment allocatedAdmin = new RoleAssignment()
            .setActorId(adminUserToCopyInto)
            .setActorIdType("IDAM")
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setRoleType(RoleType.CASE)
            .setClassification("RESTRICTED")
            .setGrantType(GrantType.SPECIFIC)
            .setRoleCategory(RoleCategory.ADMIN)
            .setRoleName("allocated-admin-caseworker")
            .setAttributes(Map.of("jurisdiction", "CIVIL",
                                   "caseType", "GENERALAPPLICATION",
                                   "caseId", gaCaseId))
            .setReadOnly(false);

        RoleAssignmentRequest expectedCopiedRole = new RoleAssignmentRequest()
            .setRoleRequest(expectedRoleRequest)
            .setRequestedRoles(List.of(allocatedJudge, leadJudge, allocatedAdmin));

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(systemUserAuth), eq(bearerToken), eq(expectedCopiedRole));
    }

    @Test
    void shouldNotCopyCaseRolesFromMainCaseIntoGaCase_if_alreadyExist() {
        when(userService.getUserInfo(bearerToken)).thenReturn(UserInfo.builder().uid(systemUserAuth).build());
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(allocatedCaseRoles);
        // GA role assignments already contain allocated-admin-caseworker
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(gaCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(prexistingGAallocatedCaseRoles);

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        RoleRequest roleRequest = new RoleRequest()
            .setAssignerId(systemUserAuth)
            .setReplaceExisting(false);

        RoleAssignment judgeAllocated = new RoleAssignment()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setRoleType(RoleType.CASE)
            .setClassification("RESTRICTED")
            .setGrantType(GrantType.SPECIFIC)
            .setRoleCategory(RoleCategory.JUDICIAL)
            .setRoleName("allocated-judge")
            .setAttributes(Map.of("jurisdiction", "CIVIL",
                                   "caseType", "GENERALAPPLICATION",
                                   "caseId", gaCaseId))
            .setReadOnly(false);

        RoleAssignment judgeLead = new RoleAssignment()
            .setActorId(judgeUserToCopyInto)
            .setActorIdType("IDAM")
            .setBeginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setEndTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .setRoleType(RoleType.CASE)
            .setClassification("RESTRICTED")
            .setGrantType(GrantType.SPECIFIC)
            .setRoleCategory(RoleCategory.JUDICIAL)
            .setRoleName("lead-judge")
            .setAttributes(Map.of("jurisdiction", "CIVIL",
                                   "caseType", "GENERALAPPLICATION",
                                   "caseId", gaCaseId))
            .setReadOnly(false);

        RoleAssignmentRequest expectedCopiedRole = new RoleAssignmentRequest()
            .setRoleRequest(roleRequest)
            .setRequestedRoles(List.of(judgeAllocated, judgeLead));

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(systemUserAuth), eq(bearerToken), eq(expectedCopiedRole));
    }

}
