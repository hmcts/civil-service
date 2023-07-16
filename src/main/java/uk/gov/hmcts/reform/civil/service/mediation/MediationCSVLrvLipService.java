package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Service
@AllArgsConstructor
public class MediationCSVLrvLipService extends MediationCSVService {

    private final OrganisationService organisationService;

    @Override
    protected MediationParams getMediationParams(CaseData caseData) {
        return MediationParams.builder()
            .applicantOrganisation(organisationService.findOrganisationById(caseData.getRespondent1OrganisationId()))
            .caseData(caseData)
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
        return caseData.getRespondent1().getPartyEmail();
    }

    @Override
    protected String getContactNumberForApplicant(MediationParams params) {
        return params.getApplicantOrganisation()
            .map(Organisation::getName)
            .orElse(params.getCaseData().getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    @Override
    protected String getContactEmailForDefendant(CaseData caseData) {
        return caseData.getRespondent1().getPartyEmail();
    }


    @Override
    protected String getCsvContactNameForDefendant(MediationParams params) {
        return getCsvIndividualName(params.getCaseData().getRespondent1());
    }

    @Override
    protected String getContactNumberForDefendant(MediationParams params) {
        return params.getCaseData().getRespondent1().getPartyPhone();
    }

}
