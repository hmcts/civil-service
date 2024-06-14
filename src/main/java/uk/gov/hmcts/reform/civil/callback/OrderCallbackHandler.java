package uk.gov.hmcts.reform.civil.callback;

import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

public abstract class OrderCallbackHandler extends DashboardWithParamsCallbackHandler {

    protected final WorkingDayIndicator workingDayIndicator;

    protected OrderCallbackHandler(DashboardApiClient dashboardApiClient, DashboardNotificationsParamsMapper mapper,
                                   FeatureToggleService featureToggleService, WorkingDayIndicator workingDayIndicator) {
        super(dashboardApiClient, mapper, featureToggleService);
        this.workingDayIndicator = workingDayIndicator;
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


    protected LocalDateTime getDateWithoutBankHolidays () {
        LocalDate date = LocalDate.now().plusDays(7);
        while (workingDayIndicator.isPublicHoliday(date)) {
            date = date.plusDays(1);
        }
        return date.atTime(16, 0);
    }
}
