package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;


@Service
@RequiredArgsConstructor
public class MediationServiceLrvLr extends MediationCSVService{

    private final OrganisationService organisationService;

    @Override
    protected MediationParams getMediationParams(CaseData caseData) {
        return MediationParams.builder()
            .applicantOrganisation(organisationService.findOrganisationById(caseData.getApplicantOrganisationId()))
            .defendantOrganisation(organisationService.findOrganisationById(caseData.getRespondent1OrganisationId()))
            .build();
    }

    @Override
    protected String getCsvContactNameForApplicant(MediationParams params) {
        return params.getApplicantOrganisation()
            .map(Organisation::getName)
            .orElse(params.getCaseData().getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    @Override
    protected String getContactEmailForApplicant(CaseData caseData) {
        return getApplicantRepresentativeEmailAddress(caseData);
    }

    @Override
    protected String getContactNumberForApplicant(MediationParams params) {
        return getRepresentativeContactNumber(params.getApplicantOrganisation());
    }

    @Override
    protected String getContactEmailForDefendant(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected String getCsvContactNameForDefendant(MediationParams params) {
        return  params.getDefendantOrganisation()
            .map(Organisation::getName)
            .orElse("");
    }

    @Override
    protected String getContactNumberForDefendant(MediationParams params) {
        return getRepresentativeContactNumber(params.getDefendantOrganisation());
    }


}
