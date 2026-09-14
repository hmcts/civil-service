package uk.gov.hmcts.reform.civil.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.exceptions.CaseAccessDataStoreCircuitOpenException;
import uk.gov.hmcts.reform.civil.exceptions.CaseAccessDataStoreUnavailableException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes = CaseAccessDataStoreService.class,
    properties = {
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.slidingWindowType=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.slidingWindowSize=2",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.minimumNumberOfCalls=2",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.slowCallRateThreshold=50",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.slowCallDurationThreshold=10ms",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.waitDurationInOpenState=60s",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.permittedNumberOfCallsInHalfOpenState=1",
        "resilience4j.circuitbreaker.instances.caseAccessDataStoreApi.automaticTransitionFromOpenToHalfOpenEnabled=false"
    }
)
@ImportAutoConfiguration({
    AopAutoConfiguration.class,
    CircuitBreakerAutoConfiguration.class
})
class CaseAccessDataStoreServiceTest {

    private static final String AUTHORISATION = "Bearer user-token";
    private static final String SERVICE_AUTHORISATION = "Bearer service-token";
    private static final List<String> CASE_IDS = List.of("1");

    @Autowired
    private CaseAccessDataStoreService caseAccessDataStoreService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        reset(caseAccessDataStoreApi);
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").reset();

        Logger loggerInstance = (Logger) LoggerFactory.getLogger(CaseAccessDataStoreService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        loggerInstance.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        Logger loggerInstance = (Logger) LoggerFactory.getLogger(CaseAccessDataStoreService.class);
        loggerInstance.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void shouldReturnResponse_whenAddCaseUserRolesIsSuccessful() {
        AddCaseAssignedUserRolesRequest request = new AddCaseAssignedUserRolesRequest();
        AddCaseAssignedUserRolesResponse expectedResponse = new AddCaseAssignedUserRolesResponse();
        when(caseAccessDataStoreApi.addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, request))
            .thenReturn(expectedResponse);

        AddCaseAssignedUserRolesResponse actualResponse = caseAccessDataStoreService.addCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            request
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(caseAccessDataStoreApi).addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, request);
    }

    @Test
    void shouldReturnResponse_whenGetUserRolesIsSuccessful() {
        CaseAssignedUserRolesResource expectedResponse = new CaseAssignedUserRolesResource();
        when(caseAccessDataStoreApi.getUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, CASE_IDS))
            .thenReturn(expectedResponse);

        CaseAssignedUserRolesResource actualResponse = caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(caseAccessDataStoreApi).getUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, CASE_IDS);
    }

    @Test
    void shouldReturnResponse_whenRemoveCaseUserRolesIsSuccessful() {
        CaseAssignedUserRolesRequest request = new CaseAssignedUserRolesRequest();
        AddCaseAssignedUserRolesResponse expectedResponse = new AddCaseAssignedUserRolesResponse();
        when(caseAccessDataStoreApi.removeCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, request))
            .thenReturn(expectedResponse);

        AddCaseAssignedUserRolesResponse actualResponse = caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            request
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(caseAccessDataStoreApi).removeCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, request);
    }

    @Test
    void shouldRethrowNotFoundException_whenAddCaseUserRolesFailsWithNotFound() {
        FeignException.NotFound notFound = new FeignException.NotFound(
            "not found",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            "not found".getBytes(UTF_8),
            Map.of()
        );
        when(caseAccessDataStoreApi.addCaseUserRoles(anyString(), anyString(), any()))
            .thenThrow(notFound);

        assertThatThrownBy(() -> caseAccessDataStoreService.addCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            new AddCaseAssignedUserRolesRequest()
        ))
            .isSameAs(notFound);
    }

    @Test
    void shouldRethrowNotFoundException_whenGetUserRolesFailsWithNotFound() {
        FeignException.NotFound notFound = new FeignException.NotFound(
            "not found",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            "not found".getBytes(UTF_8),
            Map.of()
        );
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenThrow(notFound);

        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        ))
            .isSameAs(notFound);
    }

    @Test
    void shouldRethrowNotFoundException_whenRemoveCaseUserRolesFailsWithNotFound() {
        FeignException.NotFound notFound = new FeignException.NotFound(
            "not found",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            "not found".getBytes(UTF_8),
            Map.of()
        );
        when(caseAccessDataStoreApi.removeCaseUserRoles(anyString(), anyString(), any()))
            .thenThrow(notFound);

        assertThatThrownBy(() -> caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            new CaseAssignedUserRolesRequest()
        ))
            .isSameAs(notFound);
    }

    @Test
    void shouldRecoverToClosedState_whenSuccessfulCallInHalfOpenState() {
        // 1. Open the circuit
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").transitionToOpenState();

        // 2. Transition to Half-Open
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").transitionToHalfOpenState();
        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN);

        // 3. Mock a successful call
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenReturn(new CaseAssignedUserRolesResource());

        // 4. Perform the call
        caseAccessDataStoreService.getUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, CASE_IDS);

        // 5. Verify it transitioned back to CLOSED
        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);
    }

    @Test
    void shouldHandleExceptionWithNullMessageInFallback() {
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenThrow(new RuntimeException((String) null));

        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        ))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class)
            .hasMessageContaining("CaseAccessDataStoreApi is unavailable for operation: getUserRoles")
            .extracting(Throwable::getCause)
            .isInstanceOf(RuntimeException.class);

        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .contains("CaseAccessDataStoreApi fallback invoked for operation getUserRoles. Reason: null");
    }

    @Test
    void shouldOpenCircuitBreakerOnFailedCalls() {
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenThrow(new RuntimeException("CCD /case-users failure"));

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        ))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class)
            .hasMessageContaining("CaseAccessDataStoreApi is unavailable for operation: getUserRoles")
            .hasCauseInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        ))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class)
            .hasCauseInstanceOf(RuntimeException.class);

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);

        verify(caseAccessDataStoreApi, times(2)).getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        );

        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .contains("CaseAccessDataStoreApi fallback invoked for operation getUserRoles. Reason: CCD /case-users failure");
    }

    @Test
    void shouldRouteToFallbackWithoutCallingFeignClientWhenCircuitBreakerIsOpen() {
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").transitionToOpenState();
        AddCaseAssignedUserRolesRequest caseRoleRequest = new AddCaseAssignedUserRolesRequest();

        assertThatThrownBy(() -> caseAccessDataStoreService.addCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            caseRoleRequest
        ))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class)
            .hasMessageContaining("CaseAccessDataStoreApi circuit is open for operation: addCaseUserRoles")
            .hasCauseInstanceOf(CallNotPermittedException.class);

        verify(caseAccessDataStoreApi, never()).addCaseUserRoles(anyString(), anyString(), any());

        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .contains("CaseAccessDataStoreApi circuit is OPEN for operation addCaseUserRoles. Failing fast.");
    }

    @Test
    void shouldOpenCircuitBreakerOnSlowCalls() {
        when(caseAccessDataStoreApi.removeCaseUserRoles(anyString(), anyString(), any()))
            .thenAnswer(invocation -> {
                await().pollDelay(Duration.ofMillis(50)).until(() -> true);
                return new AddCaseAssignedUserRolesResponse();
            });

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            new CaseAssignedUserRolesRequest()
        );

        caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            new CaseAssignedUserRolesRequest()
        );

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);

        CaseAssignedUserRolesRequest caseRoleRequest = new CaseAssignedUserRolesRequest();

        assertThatThrownBy(() -> caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            caseRoleRequest
        ))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class)
            .hasCauseInstanceOf(CallNotPermittedException.class);

        verify(caseAccessDataStoreApi, times(2)).removeCaseUserRoles(anyString(), anyString(), any());
    }

    @Test
    void shouldOpenCircuitBreakerOnSlowCallsForAddCaseUserRoles() {
        when(caseAccessDataStoreApi.addCaseUserRoles(anyString(), anyString(), any()))
            .thenAnswer(invocation -> {
                await().pollDelay(Duration.ofMillis(50)).until(() -> true);
                return new AddCaseAssignedUserRolesResponse();
            });

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        caseAccessDataStoreService.addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, new AddCaseAssignedUserRolesRequest());
        caseAccessDataStoreService.addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, new AddCaseAssignedUserRolesRequest());

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);
    }

    @Test
    void shouldOpenCircuitBreakerOnSlowCallsForGetUserRoles() {
        when(caseAccessDataStoreApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenAnswer(invocation -> {
                await().pollDelay(Duration.ofMillis(50)).until(() -> true);
                return new CaseAssignedUserRolesResource();
            });

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        caseAccessDataStoreService.getUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, CASE_IDS);
        caseAccessDataStoreService.getUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, CASE_IDS);

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);
    }

    @Test
    void shouldOpenCircuitBreakerOnFailedCallsForAddCaseUserRoles() {
        when(caseAccessDataStoreApi.addCaseUserRoles(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("failed"));

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        AddCaseAssignedUserRolesRequest caseRoleRequest = new AddCaseAssignedUserRolesRequest();
        assertThatThrownBy(() -> caseAccessDataStoreService.addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, caseRoleRequest))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class);

        assertThatThrownBy(() -> caseAccessDataStoreService.addCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, caseRoleRequest))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class);

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);
    }

    @Test
    void shouldOpenCircuitBreakerOnFailedCallsForRemoveCaseUserRoles() {
        when(caseAccessDataStoreApi.removeCaseUserRoles(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("failed"));

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(CLOSED);

        CaseAssignedUserRolesRequest caseRoleRequest = new CaseAssignedUserRolesRequest();
        assertThatThrownBy(() -> caseAccessDataStoreService.removeCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, caseRoleRequest))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class);

        assertThatThrownBy(() -> caseAccessDataStoreService.removeCaseUserRoles(AUTHORISATION, SERVICE_AUTHORISATION, caseRoleRequest))
            .isInstanceOf(CaseAccessDataStoreUnavailableException.class);

        assertThat(circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").getState())
            .isEqualTo(OPEN);
    }

    @Test
    void shouldUseFallbackExceptionForGetUserRolesWhenCircuitBreakerIsOpen() {
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").transitionToOpenState();

        assertThatThrownBy(() -> caseAccessDataStoreService.getUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            CASE_IDS
        ))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class)
            .hasMessageContaining("CaseAccessDataStoreApi circuit is open for operation: getUserRoles")
            .hasCauseInstanceOf(CallNotPermittedException.class);

        verify(caseAccessDataStoreApi, never()).getUserRoles(anyString(), anyString(), anyList());
    }

    @Test
    void shouldUseFallbackExceptionForRemoveCaseUserRolesWhenCircuitBreakerIsOpen() {
        circuitBreakerRegistry.circuitBreaker("caseAccessDataStoreApi").transitionToOpenState();
        CaseAssignedUserRolesRequest caseRoleRequest = new CaseAssignedUserRolesRequest();

        assertThatThrownBy(() -> caseAccessDataStoreService.removeCaseUserRoles(
            AUTHORISATION,
            SERVICE_AUTHORISATION,
            caseRoleRequest
        ))
            .isInstanceOf(CaseAccessDataStoreCircuitOpenException.class)
            .hasMessageContaining("CaseAccessDataStoreApi circuit is open for operation: removeCaseUserRoles")
            .hasCauseInstanceOf(CallNotPermittedException.class);

        verify(caseAccessDataStoreApi, never()).removeCaseUserRoles(anyString(), anyString(), any());
    }
}
