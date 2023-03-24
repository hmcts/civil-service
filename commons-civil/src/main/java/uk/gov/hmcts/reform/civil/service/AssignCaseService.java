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
        String organisationId = organisationService.findOrganisation(authorisation)
            .map(Organisation::getOrganisationIdentifier).orElse(null);
        coreCaseUserService.assignCase(
            caseId,
            userId,
            organisationId,
            caseRole.orElse(CaseRole.RESPONDENTSOLICITORONE)
        );
    }
}
