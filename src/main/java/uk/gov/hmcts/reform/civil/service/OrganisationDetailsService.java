package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationDetailsService {

    private final OrganisationService organisationService;

    public String getApplicantLegalOrganisationName(CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(caseData.getApplicantOrganisationId());
        if (caseData.getApplicant1Represented() != null && caseData.getApplicantSolicitor1ClaimStatementOfTruth() == null) {
            return getLegalOrganisationName(organisation);
        }
        return organisation.map(Organisation::getName).orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    public String getRespondent1LegalOrganisationName(CaseData caseData) {
        return getLegalOrganisationName(organisationService.findOrganisationById(
            caseData.getRespondent1OrganisationId()));
    }

    public String getRespondent2LegalOrganisationName(CaseData caseData) {
        return getLegalOrganisationName(organisationService.findOrganisationById(
            caseData.getRespondent2OrganisationId()));
    }

    private String getLegalOrganisationName(Optional<Organisation> organisation) {
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
            log.info(respondentLegalOrganizationName + " org name");
        }
        return respondentLegalOrganizationName;
    }
}
