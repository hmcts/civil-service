package uk.gov.hmcts.reform.civil.callback;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

@Slf4j
public abstract class OrderCallbackHandler extends DashboardWithParamsCallbackHandler {

    protected final WorkingDayIndicator workingDayIndicator;

    protected OrderCallbackHandler(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper,
                                   FeatureToggleService featureToggleService, WorkingDayIndicator workingDayIndicator) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.workingDayIndicator = workingDayIndicator;
    }

    protected boolean isEligibleForReconsideration(CaseData caseData) {
        return featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            && caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().compareTo(BigDecimal.valueOf(1000)) <= 0
            && (isNull(caseData.getDecisionOnRequestReconsiderationOptions())
            || !DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions()));
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
        LocalDate date = LocalDate.now();
        try {
            for (int i = 0; i < 7; i++) {
                if (workingDayIndicator.isPublicHoliday(date)) {
                    date = date.plusDays(2);
                } else {
                    date = date.plusDays(1);
                }
            }
        } catch (Exception e) {
            log.error("Error when retrieving public days");
            date = LocalDate.now().plusDays(7);
        }

        return date.atTime(16, 0, 0);
    }
}
