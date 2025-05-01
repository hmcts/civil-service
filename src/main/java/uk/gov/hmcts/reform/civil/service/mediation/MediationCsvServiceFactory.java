package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Service
@RequiredArgsConstructor
public class MediationCsvServiceFactory {

    private final MediationCSVLrvLrService mediationServiceLrvLr;
    private final MediationCSVLrvLipService mediationCSVLrvLipService;
    private final MediationCSVLipVLipService mediationCSVLipVLipService;
    private final FeatureToggleService toggleService;

    public MediationCSVService getMediationCSVService(CaseData caseData) {
        if (caseData.isRespondent1LiP() && !caseData.isApplicantLiP()) {
            return mediationCSVLrvLipService;
        } else if (caseData.isApplicantLiP() && toggleService.isLipVLipEnabled()) {
            return mediationCSVLipVLipService;
        }
        return mediationServiceLrvLr;
    }
}

