package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.exceptions.CaseAccessDataStoreUnavailableException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAccessDataStoreService {

    private static final String CIRCUIT_BREAKER_NAME = "caseAccessDataStoreApi";

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "addCaseUserRolesFallback")
    public AddCaseAssignedUserRolesResponse addCaseUserRoles(
        String authorisation,
        String serviceAuthorization,
        AddCaseAssignedUserRolesRequest caseRoleRequest
    ) {
        return caseAccessDataStoreApi.addCaseUserRoles(authorisation, serviceAuthorization, caseRoleRequest);
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUserRolesFallback")
    public CaseAssignedUserRolesResource getUserRoles(
        String authorisation,
        String serviceAuthorization,
        List<String> caseIds
    ) {
        return caseAccessDataStoreApi.getUserRoles(authorisation, serviceAuthorization, caseIds);
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "removeCaseUserRolesFallback")
    public AddCaseAssignedUserRolesResponse removeCaseUserRoles(
        String authorisation,
        String serviceAuthorization,
        CaseAssignedUserRolesRequest caseRoleRequest
    ) {
        return caseAccessDataStoreApi.removeCaseUserRoles(authorisation, serviceAuthorization, caseRoleRequest);
    }

    private AddCaseAssignedUserRolesResponse addCaseUserRolesFallback(
        String authorisation,
        String serviceAuthorization,
        AddCaseAssignedUserRolesRequest caseRoleRequest,
        Throwable throwable
    ) {
        throw unavailable("addCaseUserRoles", throwable);
    }

    private CaseAssignedUserRolesResource getUserRolesFallback(
        String authorisation,
        String serviceAuthorization,
        List<String> caseIds,
        Throwable throwable
    ) {
        throw unavailable("getUserRoles", throwable);
    }

    private AddCaseAssignedUserRolesResponse removeCaseUserRolesFallback(
        String authorisation,
        String serviceAuthorization,
        CaseAssignedUserRolesRequest caseRoleRequest,
        Throwable throwable
    ) {
        throw unavailable("removeCaseUserRoles", throwable);
    }

    private CaseAccessDataStoreUnavailableException unavailable(String operation, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) throwable;
        }

        log.warn(
            "CaseAccessDataStoreApi fallback invoked for operation {}. Failing fast. Reason: {}",
            operation,
            throwable.getMessage()
        );

        return new CaseAccessDataStoreUnavailableException(
            "CaseAccessDataStoreApi is unavailable for operation: " + operation,
            throwable
        );
    }
}
