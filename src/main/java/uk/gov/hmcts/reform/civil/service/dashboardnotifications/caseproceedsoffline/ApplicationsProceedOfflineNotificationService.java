package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class ApplicationsProceedOfflineNotificationService {

    private final ApplicationsProceedOfflineClaimantDashboardService claimantService;
    private final ApplicationsProceedOfflineDefendantDashboardService defendantService;

    public void notifyClaimant(CaseData caseData, String authToken) {
        if (shouldSkip(caseData)) {
            return;
        }
        claimantService.notify(caseData, authToken);
    }

    public void notifyDefendant(CaseData caseData, String authToken) {
        if (shouldSkip(caseData)) {
            return;
        }
        defendantService.notify(caseData, authToken);
    }

    private boolean shouldSkip(CaseData caseData) {
        return !CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.equals(caseData.getCcdState());
    }
}
