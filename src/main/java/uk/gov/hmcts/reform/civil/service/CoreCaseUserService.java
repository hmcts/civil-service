package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.exceptions.CaseAccessDataStoreUnavailableException;
import uk.gov.hmcts.reform.civil.exceptions.RetryableCaseUserException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoreCaseUserService {

    Logger log = LoggerFactory.getLogger(CoreCaseUserService.class);

    private final CaseAccessDataStoreService caseAccessDataStoreService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    @Retryable(retryFor = RetryableCaseUserException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public List<String> getUserCaseRoles(String caseId, String userId) {
        try {
            return caseAccessDataStoreService.getUserRoles(
                    getCaaAccessToken(),
                    authTokenGenerator.generate(),
                    List.of(caseId)
                )
                .getCaseAssignedUserRoles().stream()
                .filter(c -> c.getUserId().equals(userId)).distinct()
                .map(CaseAssignedUserRole::getCaseRole).toList();
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable
                 | CaseAccessDataStoreUnavailableException e) {
            throw new RetryableCaseUserException(e.getMessage(), e);
        } catch (FeignException.NotFound ex) {
            log.error("User Roles not found", ex);
            return Collections.emptyList();
        }
    }

    @Retryable(retryFor = RetryableCaseUserException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public void assignCase(String caseId, String userId, String organisationId, CaseRole caseRole) {
        try {
            String caaAccessToken = getCaaAccessToken();

            if (!userWithCaseRoleExistsOnCase(caseId, caaAccessToken, caseRole, userId)) {
                assignUserToCaseForRole(caseId, userId, organisationId, caseRole, caaAccessToken);
            } else {
                log.info("Case already have the user with {} role", caseRole.getFormattedName());
            }
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable
                 | CaseAccessDataStoreUnavailableException e) {
            throw new RetryableCaseUserException(e.getMessage(), e);
        }
    }

    @Retryable(retryFor = RetryableCaseUserException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public void unassignCase(String caseId, String userId, String organisationId, CaseRole caseRole) {
        try {
            String caaAccessToken = getCaaAccessToken();
            if (userWithCaseRoleExistsOnCase(caseId, caaAccessToken, caseRole, userId)) {
                CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation = new CaseAssignedUserRoleWithOrganisation()
                    .setCaseDataId(caseId)
                    .setUserId(userId)
                    .setCaseRole(caseRole.getFormattedName())
                    .setOrganisationId(organisationId);
                removeAccessFromRole(caseAssignedUserRoleWithOrganisation, caaAccessToken);
            }
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable
                 | CaseAccessDataStoreUnavailableException e) {
            throw new RetryableCaseUserException(e.getMessage(), e);
        }
    }

    @Retryable(retryFor = RetryableCaseUserException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public void removeCreatorRoleCaseAssignment(String caseId, String userId, String organisationId) {
        try {
            String caaAccessToken = getCaaAccessToken();

            if (userWithCaseRoleExistsOnCase(caseId, caaAccessToken, CaseRole.CREATOR, userId)) {
                removeCreatorAccess(caseId, userId, organisationId, caaAccessToken);
            } else {
                log.info("User doesn't have {} role", CaseRole.CREATOR.getFormattedName());
            }
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable
                 | CaseAccessDataStoreUnavailableException e) {
            throw new RetryableCaseUserException(e.getMessage(), e);
        }
    }

    @Recover
    public List<String> recover(RetryableCaseUserException ex, String caseId, String userId) {
        log.error("[CoreCaseUserService] Retryable User Case Roles lookup failed after retries for CaseId: {}, UserId: {}",
                  caseId, userId, ex);
        return Collections.emptyList();
    }

    @Recover
    public void recover(RetryableCaseUserException ex, String caseId, String userId, String organisationId, CaseRole caseRole) {
        log.error("[CoreCaseUserService] Retryable assignCase/unassignCase failed after retries for CaseId: {}, UserId: {}",
                  caseId, userId, ex);
    }

    @Recover
    public void recover(RetryableCaseUserException ex, String caseId, String userId, String organisationId) {
        log.error("[CoreCaseUserService] Retryable removeCreatorRoleCaseAssignment failed after retries for CaseId: {}, UserId: {}",
                  caseId, userId, ex);
    }

    @Recover
    public CaseAssignmentUserRolesResource recover(RetryableCaseUserException ex, String caseId) {
        log.error("[CoreCaseUserService] Retryable getUserRoles failed after retries for CaseId: {}", caseId, ex);
        return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(Collections.emptyList()).build();
    }

    public boolean userHasCaseRole(String caseId, String userId, CaseRole caseRole) {
        return getUserCaseRoles(caseId, userId).stream()
            .anyMatch(c -> c.equals(caseRole.getFormattedName()));
    }

    @Retryable(retryFor = RetryableCaseUserException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    public CaseAssignmentUserRolesResource getUserRoles(String caseId) {
        try {
            return caseAssignmentApi.getUserRoles(
                getCaaAccessToken(),
                authTokenGenerator.generate(),
                List.of(caseId)
            );
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable e) {
            throw new RetryableCaseUserException(e.getMessage(), e);
        } catch (FeignException.NotFound ex) {
            log.error("User Roles not found", ex);
            return CaseAssignmentUserRolesResource.builder().caseAssignmentUserRoles(Collections.emptyList()).build();
        }
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

    private void assignUserToCaseForRole(String caseId, String userId, String organisationId,
                                         CaseRole caseRole, String caaAccessToken) {
        CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation
            = new CaseAssignedUserRoleWithOrganisation()
            .setCaseDataId(caseId)
            .setUserId(userId)
            .setCaseRole(caseRole.getFormattedName())
            .setOrganisationId(organisationId);

        caseAccessDataStoreService.addCaseUserRoles(
            caaAccessToken,
            authTokenGenerator.generate(),
            new AddCaseAssignedUserRolesRequest()
                .setCaseAssignedUserRoles(List.of(caseAssignedUserRoleWithOrganisation))
        );
    }

    private void removeCreatorAccess(String caseId, String userId, String organisationId, String caaAccessToken) {
        CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation
            = new CaseAssignedUserRoleWithOrganisation()
            .setCaseDataId(caseId)
            .setUserId(userId)
            .setCaseRole(CaseRole.CREATOR.getFormattedName())
            .setOrganisationId(organisationId);

        removeAccessFromRole(caseAssignedUserRoleWithOrganisation, caaAccessToken);
    }

    private void removeAccessFromRole(CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation, String caaAccessToken) {
        caseAccessDataStoreService.removeCaseUserRoles(
            caaAccessToken,
            authTokenGenerator.generate(),
            new CaseAssignedUserRolesRequest()
                .setCaseAssignedUserRoles(List.of(caseAssignedUserRoleWithOrganisation))
        );
    }

    private boolean userWithCaseRoleExistsOnCase(String caseId, String accessToken, CaseRole caseRole, String userId) {
        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreService.getUserRoles(
            accessToken,
            authTokenGenerator.generate(),
            List.of(caseId)
        );

        return userRoles.getCaseAssignedUserRoles().stream()
            .anyMatch(c ->
                          caseRole.getFormattedName().equals(c.getCaseRole())
                              && userId.equals(c.getUserId())
            );
    }
}
