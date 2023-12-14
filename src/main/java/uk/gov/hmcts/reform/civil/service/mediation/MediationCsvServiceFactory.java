package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class MediationCsvServiceFactory {

    private final MediationCSVLrvLrService mediationServiceLrvLr;
    private final MediationCSVLrvLipService mediationCSVLrvLipService;

    public MediationCSVService getMediationCSVService(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return mediationCSVLrvLipService;
        }
        return mediationServiceLrvLr;
    }
}

