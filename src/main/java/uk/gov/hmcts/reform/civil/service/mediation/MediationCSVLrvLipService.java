package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Service
@AllArgsConstructor
public class MediationCSVLrvLipService extends MediationCSVService {

    private final OrganisationService organisationService;

    @Override
    protected ApplicantContactDetails getApplicantContactDetails() {
        return new LrApplicantContactDetails();
    }

    @Override
    protected DefendantContactDetails getDefendantContactDetails() {
        return new LipDefendantContactDetails();
    }

    @Override
    protected MediationParams getMediationParams(CaseData caseData) {
        return MediationParams.builder()
            .applicantOrganisation(organisationService.findOrganisationById(caseData.getApplicantOrganisationId()))
            .caseData(caseData)
            .build();
    }
}
