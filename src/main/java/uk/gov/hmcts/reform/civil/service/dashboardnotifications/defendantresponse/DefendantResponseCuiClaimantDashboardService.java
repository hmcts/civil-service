package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Service
public class DefendantResponseCuiClaimantDashboardService {

    private final DefendantResponseClaimantDashboardService claimantDashboardService;
    private final DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService;
    private final FeatureToggleService featureToggleService;

    public DefendantResponseCuiClaimantDashboardService(DefendantResponseClaimantDashboardService claimantDashboardService,
                                                        DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService,
                                                        FeatureToggleService featureToggleService) {
        this.claimantDashboardService = claimantDashboardService;
        this.welshClaimantDashboardService = welshClaimantDashboardService;
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendantResponse(CaseData caseData, String authToken) {
        if (isBilingualFlow(caseData)) {
            welshClaimantDashboardService.notifyDefendantResponse(caseData, authToken);
        } else {
            claimantDashboardService.notifyDefendantResponse(caseData, authToken);
        }
    }

    private boolean isBilingualFlow(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            || (featureToggleService.isWelshEnabledForMainCase() && caseData.isClaimantBilingual());
    }
}
