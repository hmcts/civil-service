package uk.gov.hmcts.reform.civil.callback;

import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

public abstract class OrderCallbackHandler extends DashboardWithParamsCallbackHandler {

    protected OrderCallbackHandler(DashboardApiClient dashboardApiClient, DashboardNotificationsParamsMapper mapper,
                                   FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    protected boolean isEligibleForReconsideration(CaseData caseData) {
        return caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().intValue() <= BigDecimal.valueOf(10000).intValue();
    }

    protected boolean hasTrackChanged(CaseData caseData) {
        return SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData))
            && !caseData.isSmallClaim();
    }

    protected AllocatedTrack getPreviousAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null,
            null
        );
    }

    protected boolean isCarmApplicableCase(CaseData caseData) {
        return getFeatureToggleService().isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.equals(getPreviousAllocatedTrack(caseData));
    }

}
