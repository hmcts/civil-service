package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganisationDetailsService {

    private final OrganisationService organisationService;

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(caseData.getApplicantOrganisationId());
        return organisation.map(Organisation::getName).orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

}
