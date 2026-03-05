package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

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
        MediationParams mediationParams = new MediationParams();
        mediationParams.setCaseData(caseData);
        return mediationParams;
    }
}
