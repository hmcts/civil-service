package uk.gov.hmcts.reform.civil.service.roleassignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import static uk.gov.hmcts.reform.civil.service.roleassignment.RolesAndAccessAssignmentService.ROLE_NAMES;
import static uk.gov.hmcts.reform.civil.service.roleassignment.RolesAndAccessAssignmentService.ROLE_TYPE;

@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(userService.getUserInfo(bearerToken)).thenReturn(UserInfo.builder().uid(systemUserAuth).build());

        RoleAssignmentResponse allocatedJudgeRoleAssignment = RoleAssignmentResponse.builder()
            .actorId(judgeUserToCopyInto)
            .actorIdType("IDAM")
            .roleType("CASE")
            .roleName("allocated-judge")
            .roleLabel("Allocated Judge")
            .classification("RESTRICTED")
            .grantType("SPECIFIC")
            .roleCategory("JUDICIAL")
            .readOnly(false)
            .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .created(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .attributes(Attributes.builder()
                            .substantive("Y")
                            .caseId(mainCaseId)
                            .caseType("CIVIL")
                            .primaryLocation(null)
                            .contractType(null)
                            .region(null)
                            .build())
            .build();
        RoleAssignmentResponse leadJudgeRoleAssignment = RoleAssignmentResponse.builder()
            .actorId(judgeUserToCopyInto)
            .actorIdType("IDAM")
            .roleType("CASE")
            .roleName("lead-judge")
            .roleLabel("Lead Judge")
            .classification("RESTRICTED")
            .grantType("SPECIFIC")
            .roleCategory("JUDICIAL")
            .readOnly(false)
            .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .created(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .attributes(Attributes.builder()
                            .substantive("Y")
                            .caseId(mainCaseId)
                            .caseType("CIVIL")
                            .primaryLocation(null)
                            .contractType(null)
                            .region(null)
                            .build())
            .build();
        RoleAssignmentResponse adminRoleAssignment = RoleAssignmentResponse.builder()
            .actorId(adminUserToCopyInto)
            .actorIdType("IDAM")
            .roleType("CASE")
            .roleName("allocated-admin-caseworker")
            .roleLabel("allocated admin")
            .classification("RESTRICTED")
            .grantType("SPECIFIC")
            .roleCategory("ADMIN")
            .readOnly(false)
            .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .created(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .attributes(Attributes.builder()
                            .substantive("Y")
                            .caseId(mainCaseId)
                            .caseType("CIVIL")
                            .region(null)
                            .build())
            .build();

        List<RoleAssignmentResponse> roleList = new ArrayList<>();
        roleList.add(allocatedJudgeRoleAssignment);
        roleList.add(leadJudgeRoleAssignment);
        roleList.add(adminRoleAssignment);
        allocatedCaseRoles = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(roleList).build();

        // pre-existing copies should not be re-copied
        RoleAssignmentResponse preExistingCopiedAdminRoleAssignment = RoleAssignmentResponse.builder()
            .actorId(adminUserToCopyInto)
            .actorIdType("IDAM")
            .roleType("CASE")
            .roleName("allocated-admin-caseworker")
            .roleLabel("allocated admin")
            .classification("RESTRICTED")
            .grantType("SPECIFIC")
            .roleCategory("ADMIN")
            .readOnly(false)
            .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .created(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
            .attributes(Attributes.builder()
                            .substantive("Y")
                            .caseId(gaCaseId)
                            .caseType("GENERALAPPLICATION")
                            .region(null)
                            .build())
            .build();

        List<RoleAssignmentResponse> existingGaRoleList = new ArrayList<>();
        existingGaRoleList.add(preExistingCopiedAdminRoleAssignment);
        prexistingGAallocatedCaseRoles = RoleAssignmentServiceResponse.builder().roleAssignmentResponse(existingGaRoleList).build();
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
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(allocatedCaseRoles);
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(gaCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(new RoleAssignmentServiceResponse(Collections.emptyList()));

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        RoleAssignmentRequest expectedCopiedRole = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId(systemUserAuth)
                             .replaceExisting(false)
                             .build())
            .requestedRoles(List.of(
                RoleAssignment.builder()
                    .actorId(judgeUserToCopyInto)
                    .actorIdType("IDAM")
                    .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                    .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                    .roleType(RoleType.CASE)
                    .classification("RESTRICTED")
                    .grantType(GrantType.SPECIFIC)
                    .roleCategory(RoleCategory.JUDICIAL)
                    .roleName("allocated-judge")
                    .attributes(Map.of("jurisdiction", "CIVIL",
                                       "caseType", "GENERALAPPLICATION",
                                       "caseId", gaCaseId))
                    .readOnly(false)
                    .build(),
                RoleAssignment.builder()
                    .actorId(judgeUserToCopyInto)
                    .actorIdType("IDAM")
                    .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                    .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                    .roleType(RoleType.CASE)
                    .classification("RESTRICTED")
                    .grantType(GrantType.SPECIFIC)
                    .roleCategory(RoleCategory.JUDICIAL)
                    .roleName("lead-judge")
                    .attributes(Map.of("jurisdiction", "CIVIL",
                                       "caseType", "GENERALAPPLICATION",
                                       "caseId", gaCaseId))
                    .readOnly(false)
                    .build(),
                RoleAssignment.builder()
                    .actorId(adminUserToCopyInto)
                    .actorIdType("IDAM")
                    .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                    .roleType(RoleType.CASE)
                    .classification("RESTRICTED")
                    .grantType(GrantType.SPECIFIC)
                    .roleCategory(RoleCategory.ADMIN)
                    .roleName("allocated-admin-caseworker")
                    .attributes(Map.of("jurisdiction", "CIVIL",
                                       "caseType", "GENERALAPPLICATION",
                                       "caseId", gaCaseId))
                    .readOnly(false)
                    .build()

            )
            ).build();

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(systemUserAuth), eq(bearerToken), eq(expectedCopiedRole));
    }

    @Test
    void shouldNotCopyCaseRolesFromMainCaseIntoGaCase_if_alreadyExist() {
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(mainCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(allocatedCaseRoles);
        // GA role assignments already contain allocated-admin-caseworker
        when(roleAssignmentService.queryRoleAssignmentsByCaseIdAndRole(eq(gaCaseId), eq(ROLE_TYPE), eq(ROLE_NAMES), eq(bearerToken)))
            .thenReturn(prexistingGAallocatedCaseRoles);

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(mainCaseId, gaCaseId);

        RoleAssignmentRequest expectedCopiedRole = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId(systemUserAuth)
                             .replaceExisting(false)
                             .build())
            .requestedRoles(List.of(
                                RoleAssignment.builder()
                                    .actorId(judgeUserToCopyInto)
                                    .actorIdType("IDAM")
                                    .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                                    .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                                    .roleType(RoleType.CASE)
                                    .classification("RESTRICTED")
                                    .grantType(GrantType.SPECIFIC)
                                    .roleCategory(RoleCategory.JUDICIAL)
                                    .roleName("allocated-judge")
                                    .attributes(Map.of("jurisdiction", "CIVIL",
                                                       "caseType", "GENERALAPPLICATION",
                                                       "caseId", gaCaseId))
                                    .readOnly(false)
                                    .build(),
                                RoleAssignment.builder()
                                    .actorId(judgeUserToCopyInto)
                                    .actorIdType("IDAM")
                                    .beginTime(ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                                    .endTime(ZonedDateTime.of(2025, 1, 7, 12, 0, 0, 0, ZoneId.of("Europe/London")))
                                    .roleType(RoleType.CASE)
                                    .classification("RESTRICTED")
                                    .grantType(GrantType.SPECIFIC)
                                    .roleCategory(RoleCategory.JUDICIAL)
                                    .roleName("lead-judge")
                                    .attributes(Map.of("jurisdiction", "CIVIL",
                                                       "caseType", "GENERALAPPLICATION",
                                                       "caseId", gaCaseId))
                                    .readOnly(false)
                                    .build()
                            )
            ).build();

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(systemUserAuth), eq(bearerToken), eq(expectedCopiedRole));
    }

}
