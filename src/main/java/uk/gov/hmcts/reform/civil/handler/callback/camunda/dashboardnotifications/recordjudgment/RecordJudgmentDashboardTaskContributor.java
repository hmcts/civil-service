package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.recordjudgment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class RecordJudgmentDashboardTaskContributor extends DashboardTaskContributor {

    public RecordJudgmentDashboardTaskContributor(
        RecordJudgmentDefendantDashboardTask defendantTask
    ) {
        super(
            DashboardTaskIds.RECORD_JUDGMENT,
            defendantTask
        );
    }
}
