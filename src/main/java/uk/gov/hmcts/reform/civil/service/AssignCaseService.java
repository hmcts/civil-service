package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignCaseService {

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final OrganisationService organisationService;

    public void assignCase(String authorisation, String caseId, Optional<CaseRole> caseRole) {
        String userId = userService.getUserInfo(authorisation).getUid();
        String organisationId = getOrganisationId(authorisation, caseRole);
        coreCaseUserService.assignCase(
            caseId,
            userId,
            organisationId,
            caseRole.orElse(CaseRole.RESPONDENTSOLICITORONE)
        );
    }

    private String getOrganisationId(String authorisation, Optional<CaseRole> caseRole) {
        String id = null;
        if (caseRole.map(CaseRole::isProfessionalRole).orElse(false)) {
            try {
                id = organisationService.findOrganisation(authorisation)
                    .map(Organisation::getOrganisationIdentifier).orElse(null);
            } catch (Exception e) {
                log.error("Error getting organisation id", e);
            }
        }
        return id;
    }

}
