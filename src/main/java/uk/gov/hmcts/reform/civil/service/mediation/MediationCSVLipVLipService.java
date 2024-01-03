package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Service
@AllArgsConstructor
public class MediationCSVLipVLipService extends MediationCSVService {

    @Override
    protected ApplicantContactDetails getApplicantContactDetails() {
        return new LipApplicantContactDetails();
    }

    @Override
    protected DefendantContactDetails getDefendantContactDetails() {
        return new LipDefendantContactDetails();
    }

    @Override
    protected MediationParams getMediationParams(CaseData caseData) {
        return MediationParams.builder()
                .caseData(caseData)
                .build();
    }
}
