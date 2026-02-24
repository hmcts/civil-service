package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Service
@RequiredArgsConstructor
public class MediationCSVLrvLrService extends MediationCSVService {

    private final OrganisationService organisationService;

    @Override
    protected ApplicantContactDetails getApplicantContactDetails() {
        return new LrApplicantContactDetails();
    }

    @Override
    protected DefendantContactDetails getDefendantContactDetails() {
        return new LrDefendantContactDetails();
    }

    @Override
    protected MediationParams getMediationParams(CaseData caseData) {
        MediationParams mediationParams = new MediationParams();
        mediationParams.setApplicantOrganisation(
            organisationService.findOrganisationById(caseData.getApplicantOrganisationId())
        );
        mediationParams.setDefendantOrganisation(
            organisationService.findOrganisationById(caseData.getRespondent1OrganisationId())
        );
        mediationParams.setCaseData(caseData);
        return mediationParams;
    }
}
