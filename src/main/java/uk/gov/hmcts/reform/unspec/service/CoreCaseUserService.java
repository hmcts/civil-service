package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.unspec.enums.CaseRole;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoreCaseUserService {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final IdamClient idamClient;
    private final OrganisationService organisationService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    public void assignCase(String caseId, String caseUserToken) {
        String userId = idamClient.getUserInfo(caseUserToken).getUid();
        Optional<Organisation> organisation = organisationService.findOrganisation(caseUserToken);

        String accessToken = idamClient.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );

        CaseAssignedUserRoleWithOrganisation caseAssignedUserRoleWithOrganisation
            = CaseAssignedUserRoleWithOrganisation.builder()
            .caseDataId(caseId)
            .userId(userId)
            .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
            .organisationId(organisation.map(Organisation::getOrganisationIdentifier).orElse(null))
            .build();

        caseAccessDataStoreApi.addCaseUserRoles(
            accessToken,
            authTokenGenerator.generate(),
            AddCaseAssignedUserRolesRequest.builder()
                .caseAssignedUserRoles(List.of(caseAssignedUserRoleWithOrganisation))
                .build()
        );
    }
}
