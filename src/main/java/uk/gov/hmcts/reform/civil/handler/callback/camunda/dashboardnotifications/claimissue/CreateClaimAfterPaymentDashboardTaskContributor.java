package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CreateClaimAfterPaymentDashboardTaskContributor extends DashboardTaskContributor {

    public CreateClaimAfterPaymentDashboardTaskContributor(
        CreateClaimAfterPaymentDefendantDashboardTask defendantTask,
        CreateClaimAfterPaymentApplicantDashboardTask applicantTask
    ) {
        super(
            DashboardTaskIds.CREATE_CLAIM_AFTER_PAYMENT,
            defendantTask,
            applicantTask
        );
    }
}
