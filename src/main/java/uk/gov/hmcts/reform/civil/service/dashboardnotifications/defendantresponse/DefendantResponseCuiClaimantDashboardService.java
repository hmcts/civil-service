package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
public class DefendantResponseCuiClaimantDashboardService {

    private final DefendantResponseClaimantDashboardService claimantDashboardService;
    private final DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService;

    public DefendantResponseCuiClaimantDashboardService(DefendantResponseClaimantDashboardService claimantDashboardService,
                                                        DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService) {
        this.claimantDashboardService = claimantDashboardService;
        this.welshClaimantDashboardService = welshClaimantDashboardService;
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
            || caseData.isClaimantBilingual();
    }
}
