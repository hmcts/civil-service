package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.exceptions.CaseAccessDataStoreCircuitOpenException;

import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {
    CoreCaseUserServiceIT.TestConfiguration.class,
    CaseAccessDataStoreService.class
})
@ImportAutoConfiguration({
    AopAutoConfiguration.class,
    CircuitBreakerAutoConfiguration.class
})
@SuppressWarnings({"java:S5960", "java:S6813"})
class CoreCaseUserServiceIT {

    private static final String CAA_USER_AUTH_TOKEN = "Bearer caa-user-xyz";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-xyz";
    private static final String CASE_ID = "1";
    private static final String USER_ID = "User1";
    private static final String USER_ID2 = "User2";

    private final FeignException gatewayTimeoutException = new FeignException.GatewayTimeout(
        "gateway timeout message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "gateway timeout response body".getBytes(UTF_8),
        Map.of());

    private final FeignException badGatewayException = new FeignException.BadGateway(
        "bad gateway message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "bad gateway response body".getBytes(UTF_8),
        Map.of());

    private final FeignException notFoundException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8),
        Map.of());

    @Configuration
    @EnableRetry
    static class TestConfiguration {
        @Bean
        CoreCaseUserService coreCaseUserService(
            CaseAccessDataStoreService caseAccessDataStoreService,
            CaseAssignmentApi caseAssignmentApi,
            UserService userService,
            CrossAccessUserConfiguration crossAccessUserConfiguration,
            AuthTokenGenerator authTokenGenerator
        ) {
            return new CoreCaseUserService(
                caseAccessDataStoreService,
                caseAssignmentApi,
                userService,
                crossAccessUserConfiguration,
                authTokenGenerator
            );
        }
    }

    @Autowired
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private CaseAccessDataStoreService caseAccessDataStoreService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private UserService userService;

    @MockBean
    private CrossAccessUserConfiguration crossAccessUserConfiguration;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").reset();
        when(crossAccessUserConfiguration.getUserName()).thenReturn("cross-access-user");
        when(crossAccessUserConfiguration.getPassword()).thenReturn("cross-access-password");
        when(userService.getAccessToken("cross-access-user", "cross-access-password")).thenReturn(CAA_USER_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldRetryAndEventuallyReturnRolesOnTransientFailures() {
        CaseAssignedUserRole role1 = new CaseAssignedUserRole()
            .setUserId(USER_ID)
            .setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        CaseAssignedUserRole role2 = new CaseAssignedUserRole()
            .setUserId(USER_ID2)
            .setCaseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());

        CaseAssignedUserRolesResource caseAssignedUserRolesResource = new CaseAssignedUserRolesResource()
            .setCaseAssignedUserRoles(List.of(role1, role2));

        doThrow(gatewayTimeoutException)
            .doThrow(badGatewayException)
            .doReturn(caseAssignedUserRolesResource)
            .when(caseAccessDataStoreApi).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));

        assertThat(AopUtils.isAopProxy(coreCaseUserService)).isTrue();

        var roles = coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID);

        verify(caseAccessDataStoreApi, times(3)).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));
        assertThat(roles).containsExactly(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
    }

    @Test
    void shouldReturnEmptyListWhenRetriesExhausted() {
        doThrow(gatewayTimeoutException)
            .when(caseAccessDataStoreApi).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));

        var roles = coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID);

        verify(caseAccessDataStoreApi, times(3)).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));
        assertThat(roles).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWithoutRetryWhenNotFound() {
        doThrow(notFoundException)
            .when(caseAccessDataStoreApi).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));

        var roles = coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID);

        verify(caseAccessDataStoreApi).getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));
        assertThat(roles).isEmpty();
    }

    @Test
    void shouldOpenCircuitAfterMultipleFailures() {
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenThrow(gatewayTimeoutException);

        // Call 1: results in 3 failures due to @Retryable. Circuit still CLOSED (minimumNumberOfCalls is 5)
        coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID);
        verify(caseAccessDataStoreApi, times(3)).getUserRoles(anyString(), anyString(), anyList());

        // Call 2: results in 2 more failures, then circuit should OPEN.
        // The 3rd attempt in this call will fail fast with CaseAccessDataStoreCircuitOpenException
        assertThatThrownBy(() -> coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class);

        // The circuit should now be OPEN. Subsequent calls should fail fast with CaseAccessDataStoreCircuitOpenException
        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID)))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class)
            .hasCauseInstanceOf(CallNotPermittedException.class);

        // API should not have been called again after the 5th failure
        verify(caseAccessDataStoreApi, times(5)).getUserRoles(anyString(), anyString(), anyList());
    }

    @Test
    void shouldNotRetryWhenCircuitIsOpen() {
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenThrow(gatewayTimeoutException);

        // Open the circuit
        for (int i = 0; i < 5; i++) {
            try {
                caseAccessDataStoreService.getUserRoles(CAA_USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, List.of(CASE_ID));
            } catch (Exception ignored) {
                //Do nothing
            }
        }
        verify(caseAccessDataStoreApi, times(5)).getUserRoles(anyString(), anyString(), anyList());

        // Now call via CoreCaseUserService which has @Retryable
        assertThatThrownBy(() -> coreCaseUserService.getUserCaseRoles(CASE_ID, USER_ID))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class);

        // If it had retried, we would see multiple calls to the service (though they would fail fast)
        // However, we want to ensure the API wasn't called again.
        verify(caseAccessDataStoreApi, times(5)).getUserRoles(anyString(), anyString(), anyList());
    }
}
