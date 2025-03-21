package uk.gov.hmcts.reform.civil.service.userroleassignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.service.roleassignment.RoleAssignmentInitialisationService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleInitialisationServiceTest {

    private static final String USER_AUTH = "user-auth";
    private static final String USER_ID = "user-id";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @Mock
    private SystemUpdateUserConfiguration systemUserConfig;
    @Mock
    private RoleAssignmentsService roleAssignmentService;
    @Mock
    private UserService userService;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private RoleAssignmentInitialisationService roleInitialisationService;

    @BeforeEach
    void setUpTests() {
        when(systemUserConfig.getUserName()).thenReturn(USERNAME);
        when(systemUserConfig.getPassword()).thenReturn(PASSWORD);
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(USER_AUTH);
        when(userService.getUserInfo(USER_AUTH)).thenReturn(UserInfo.builder().uid(USER_ID).build());
    }

    @Test
    void shouldCallRoleAssignmentServiceWithExpectedRequest() {
        RoleAssignmentRequest expected = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId(USER_ID)
                             .reference("civil-hearings-system-user")
                             .process("civil-system-user")
                             .replaceExisting(true)
                             .build())
            .requestedRoles(List.of(
                RoleAssignment.builder()
                    .actorId(USER_ID)
                    .actorIdType("IDAM")
                    .roleType(RoleType.ORGANISATION)
                    .classification("PUBLIC")
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.SYSTEM)
                    .roleName("hearing-manager")
                    .attributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
                    .readOnly(false)
                    .build(),
                RoleAssignment.builder()
                    .actorId(USER_ID)
                    .actorIdType("IDAM")
                    .roleType(RoleType.ORGANISATION)
                    .classification("PUBLIC")
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.SYSTEM)
                    .roleName("hearing-viewer")
                    .attributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
                    .readOnly(false)
                    .build()
            )).build();

        roleInitialisationService.initialiseUserRolesOnStartUp();

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(USER_ID), eq(USER_AUTH), eq(expected));
    }

    @Test
    void shouldCallRoleAssignmentServiceWithExpectedRequest_caseAllocator() {
        RoleAssignmentRequest expected = RoleAssignmentRequest.builder()
            .roleRequest(RoleRequest.builder()
                             .assignerId(USER_ID)
                             .reference("civil-case-allocator-system-user")
                             .process("civil-system-user")
                             .replaceExisting(true)
                             .build())
            .requestedRoles(List.of(
                RoleAssignment.builder()
                    .actorId(USER_ID)
                    .actorIdType("IDAM")
                    .roleType(RoleType.ORGANISATION)
                    .classification("PUBLIC")
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.SYSTEM)
                    .roleName("case-allocator")
                    .attributes(Map.of("jurisdiction", "CIVIL", "caseType", "CIVIL"))
                    .readOnly(false)
                    .build(),
                RoleAssignment.builder()
                    .actorId(USER_ID)
                    .actorIdType("IDAM")
                    .roleType(RoleType.ORGANISATION)
                    .classification("PUBLIC")
                    .grantType(GrantType.STANDARD)
                    .roleCategory(RoleCategory.SYSTEM)
                    .roleName("case-allocator")
                    .attributes(Map.of("jurisdiction", "CIVIL", "caseType", "GENERALAPPLICATION"))
                    .readOnly(false)
                    .build()
            )).build();

        roleInitialisationService.initialiseUserRolesOnStartUp();

        verify(roleAssignmentService, times(1))
            .assignUserRoles(eq(USER_ID), eq(USER_AUTH), eq(expected));
    }

}
