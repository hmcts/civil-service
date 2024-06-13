package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseAssignmentSupportService {

    private final CaseAssignmentApi caseAssignmentApi;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public void unAssignUserFromCases(List<String> caseIds, String organisationId, String userId) {
        String authToken = authTokenGenerator.generate();
        String caaAccessToken = getCaaAccessToken();

        List<CaseAssignmentUserRole> userRoles =
            caseAssignmentApi.getUserRoles(caaAccessToken, authToken, caseIds).getCaseAssignmentUserRoles().stream()
                .filter(role -> role.getUserId().equals(userId)).toList();

        List<CaseAssignmentUserRoleWithOrganisation> userRolesWithOrganisation =
            userRoles.stream().map(role -> CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(role.getCaseDataId())
                .caseRole(role.getCaseRole())
                .userId(role.getUserId())
                .organisationId(organisationId)
                .build()
            ).toList();

        CaseAssignmentUserRolesRequest request = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(userRolesWithOrganisation).build();
        caseAssignmentApi.removeCaseUserRoles(caaAccessToken, authToken, request);
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }
}

