package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.recordjudgment;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class RecordJudgmentDashboardTaskContributor extends DashboardTaskContributor {

    public RecordJudgmentDashboardTaskContributor(RecordJudgmentDefendantDashboardTask defendantTask) {

        super(
            DashboardTaskIds.RECORD_JUDGMENT,
            defendantTask
        );

    }
}
