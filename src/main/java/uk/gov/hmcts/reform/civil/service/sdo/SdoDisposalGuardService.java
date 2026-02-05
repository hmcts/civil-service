package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class SdoDisposalGuardService {

    private final SdoFeatureToggleService featureToggleService;

    public boolean shouldBlockPrePopulate(CaseData caseData) {
        return shouldBlockDisposalOrders(caseData);
    }

    public boolean shouldBlockOrderDetails(CaseData caseData) {
        return shouldBlockDisposalOrders(caseData);
    }

    private boolean shouldBlockDisposalOrders(CaseData caseData) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return false;
        }

        return featureToggleService.isMultiOrIntermediateTrackCase(caseData)
            && OrderType.DISPOSAL.equals(caseData.getOrderType())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState());
    }
}
