package uk.gov.hmcts.reform.civil.service.user;

import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;
import uk.gov.hmcts.reform.civil.exceptions.UserNotFoundOnCaseException;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserInformationServiceTest {

    public static final UserInfo USER_INFO = UserInfo.builder().uid("uid").build();
    public static final String AUTHORIZATION = "token";
    public static final String CASE_ID = "123";

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @InjectMocks
    private UserInformationService userInformationService;

    @Test
    public void should_getUserCaseRoles_Success() {
        List<String> expectedRoles = List.of("role1", "role2");

        when(userService.getUserInfo(AUTHORIZATION)).thenReturn(USER_INFO);
        when(coreCaseUserService.getUserCaseRoles(CASE_ID, USER_INFO.getUid())).thenReturn(expectedRoles);

        List<String> actualRoles = userInformationService.getUserCaseRoles(CASE_ID, AUTHORIZATION);

        assertEquals(expectedRoles, actualRoles);
        verify(userService, times(1)).getUserInfo(AUTHORIZATION);
        verify(coreCaseUserService, times(1)).getUserCaseRoles(CASE_ID, USER_INFO.getUid());
    }

    @Test
    public void should_getUserCaseRoles_throw_case_not_found() {

        when(userService.getUserInfo(AUTHORIZATION)).thenThrow(new CaseNotFoundException());

        assertThrows(CaseNotFoundException.class, () -> userInformationService.getUserCaseRoles(CASE_ID, AUTHORIZATION));
        verify(userService, times(1)).getUserInfo(AUTHORIZATION);
        verify(coreCaseUserService, never()).getUserCaseRoles(anyString(), anyString());
    }

    @Test
    public void should_getUserCaseRoles_throw_upstream_unavailable_for_ccd_case_users_failure() {
        RemoteProcessEngineException cause = new RemoteProcessEngineException("CCD failed", new Throwable());

        when(userService.getUserInfo(AUTHORIZATION)).thenReturn(USER_INFO);
        when(coreCaseUserService.getUserCaseRoles(CASE_ID, USER_INFO.getUid())).thenThrow(cause);

        UpstreamUnavailableException exception = assertThrows(
            UpstreamUnavailableException.class,
            () -> userInformationService.getUserCaseRoles(CASE_ID, AUTHORIZATION)
        );

        assertEquals("CCD case-users is currently unavailable", exception.getMessage());
        assertEquals("CCD case-users", exception.getUpstreamService());
        assertEquals(CASE_ID, exception.getCaseId());
        assertEquals(USER_INFO.getUid(), exception.getUserId());
        assertSame(cause, exception.getCause());
        verify(userService, times(1)).getUserInfo(AUTHORIZATION);
        verify(coreCaseUserService, times(1)).getUserCaseRoles(CASE_ID, USER_INFO.getUid());
    }

    @Test
    public void should_getUserCaseRoles_throw_user_not_found_on_case_exception() {

        List<String> expectedRoles = List.of();

        when(userService.getUserInfo(AUTHORIZATION)).thenReturn(USER_INFO);
        when(coreCaseUserService.getUserCaseRoles(CASE_ID, USER_INFO.getUid())).thenReturn(expectedRoles);

        assertThrows(UserNotFoundOnCaseException.class, () -> userInformationService.getUserCaseRoles(CASE_ID, AUTHORIZATION));
        verify(userService, times(1)).getUserInfo(AUTHORIZATION);
        verify(coreCaseUserService, times(1)).getUserCaseRoles(CASE_ID, USER_INFO.getUid());
    }
}
