package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.citizenhearingfeepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CitizenHearingFeePaymentDashboardTaskContributor extends DashboardTaskContributor {

    public CitizenHearingFeePaymentDashboardTaskContributor(CitizenHearingFeePaymentDashboardTask task) {
        super(DashboardTaskIds.CITIZEN_HEARING_FEE_PAYMENT, task);
    }
}
