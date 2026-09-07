package uk.gov.hmcts.reform.civil.callback;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoReconsiderationDeadlineService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

public abstract class OrderCallbackHandler extends DashboardWithParamsCallbackHandler {

    protected final SdoReconsiderationDeadlineService reconsiderationDeadlineService;

    protected OrderCallbackHandler(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper,
                                   FeatureToggleService featureToggleService,
                                   SdoReconsiderationDeadlineService reconsiderationDeadlineService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.reconsiderationDeadlineService = reconsiderationDeadlineService;
    }

    protected boolean isEligibleForReconsideration(CaseData caseData) {
        return reconsiderationDeadlineService.isEligibleForReconsideration(caseData);
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

    protected LocalDateTime getDateWithoutBankHolidays() {
        return reconsiderationDeadlineService.calculateReconsiderationDeadline();
    }
}
