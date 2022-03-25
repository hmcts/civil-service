package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseAssignmentSupportService {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public void unAssignUserFromCases(List<String> caseIds, String organisationId, String userId) {
        String authToken = authTokenGenerator.generate();
        String caaAccessToken = getCaaAccessToken();

        List<CaseAssignedUserRole> userRoles =
            caseAccessDataStoreApi.getUserRoles(caaAccessToken, authToken, caseIds).getCaseAssignedUserRoles().stream()
                .filter(role -> role.getUserId().equals(userId)).collect(Collectors.toList());

        List<CaseAssignedUserRoleWithOrganisation> userRolesWithOrganisation =
            userRoles.stream().map(role -> CaseAssignedUserRoleWithOrganisation.builder()
                .caseDataId(role.getCaseDataId())
                .caseRole(role.getCaseRole())
                .userId(role.getUserId())
                .organisationId(organisationId)
                .build()
            ).collect(Collectors.toList());

        CaseAssignedUserRolesRequest request = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(userRolesWithOrganisation).build();
        caseAccessDataStoreApi.removeCaseUserRoles(caaAccessToken, authToken, request);
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }
}

