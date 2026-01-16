package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreCaseUserService {

    Logger log = LoggerFactory.getLogger(CoreCaseUserService.class);

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final CaseAssignmentApi caseAssignmentApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> getUserCaseRoles(String caseId, String userId) {
        return caseAccessDataStoreApi.getUserRoles(getCaaAccessToken(), authTokenGenerator.generate(), List.of(caseId))
            .getCaseAssignedUserRoles().stream()
            .filter(c -> c.getUserId().equals(userId)).distinct()
            .map(c -> c.getCaseRole()).collect(Collectors.toList());
    }

    public void assignCase(String caseId, String userId, String organisationId, CaseRole caseRole) {
        String caaAccessToken = getCaaAccessToken();

        if (!userWithCaseRoleExistsOnCase(caseId, caaAccessToken, caseRole)) {
            assignUserToCaseForRole(caseId, userId, organisationId, caseRole, caaAccessToken);
        } else {
            log.info("Case already have the user with {} role", caseRole.getFormattedName());
        }
    }

    public void unassignCase(String caseId, String userId, String organisationId, CaseRole caseRole) {
        String caaAccessToken = getCaaAccessToken();
        if (userWithCaseRoleExistsOnCase(caseId, caaAccessToken, caseRole)) {
            CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation = new CaseAssignedUserRoleWithOrganisation()
                .setCaseDataId(caseId)
                .setUserId(userId)
                .setCaseRole(caseRole.getFormattedName())
                .setOrganisationId(organisationId);
            removeAccessFromRole(caseAssignedUserRoleWithOrganisation, caaAccessToken);
        }
    }

    public void removeCreatorRoleCaseAssignment(String caseId, String userId, String organisationId) {

        String caaAccessToken = getCaaAccessToken();

        if (userWithCaseRoleExistsOnCase(caseId, caaAccessToken, CaseRole.CREATOR)) {
            removeCreatorAccess(caseId, userId, organisationId, caaAccessToken);
        } else {
            log.info("User doesn't have {} role", CaseRole.CREATOR.getFormattedName());
        }
    }

    public boolean userHasCaseRole(String caseId, String userId, CaseRole caseRole) {
        return getUserCaseRoles(caseId, userId).stream()
            .anyMatch(c -> c.equals(caseRole.getFormattedName()));
    }

    public CaseAssignmentUserRolesResource getUserRoles(String caseId) {
        return caseAssignmentApi.getUserRoles(
            getCaaAccessToken(),
            authTokenGenerator.generate(),
            List.of(caseId)
        );
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

        caseAccessDataStoreApi.addCaseUserRoles(
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
        caseAccessDataStoreApi.removeCaseUserRoles(
            caaAccessToken,
            authTokenGenerator.generate(),
            new CaseAssignedUserRolesRequest()
                .setCaseAssignedUserRoles(List.of(caseAssignedUserRoleWithOrganisation))
        );
    }

    private boolean userWithCaseRoleExistsOnCase(String caseId, String accessToken, CaseRole caseRole) {
        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
            accessToken,
            authTokenGenerator.generate(),
            List.of(caseId)
        );

        return userRoles.getCaseAssignedUserRoles().stream()
            .anyMatch(c -> c.getCaseRole().equals(caseRole.getFormattedName()));
    }
}
