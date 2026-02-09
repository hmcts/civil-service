package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingfeeunpaid;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class HearingFeeUnpaidDashboardTaskContributor extends DashboardTaskContributor {

    public HearingFeeUnpaidDashboardTaskContributor(HearingFeeUnpaidClaimantDashboardTask claimantDashboardTask,
                                                    HearingFeeUnpaidDefendantDashboardTask defendantDashboardTask) {
        super(
            DashboardTaskIds.HEARING_FEE_UNPAID,
            claimantDashboardTask,
            defendantDashboardTask
        );
    }
}
