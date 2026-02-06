package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationissued.ApplicationIssuedApplicantDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationissued.ApplicationIssuedDashboardTaskContributor;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationissued.ApplicationIssuedRespondentDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted.ApplicationSubmittedApplicantDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted.ApplicationSubmittedDashboardTaskContributor;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted.ApplicationSubmittedRespondentDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.hwf.HwfOutcomeApplicantDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.hwf.HwfOutcomeDashboardTaskContributor;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision.MakeDecisionApplicantDashboardTask;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision.MakeDecisionDashboardTaskContributor;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision.MakeDecisionRespondentDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GaDashboardTaskContributorsTest {

    @Test
    void shouldRegisterGaContributorsWithExpectedTaskIds() {
        List<ContributorSpec> specs = List.of(
            new ContributorSpec(
                new ApplicationIssuedDashboardTaskContributor(
                    mock(ApplicationIssuedApplicantDashboardTask.class),
                    mock(ApplicationIssuedRespondentDashboardTask.class)
                ),
                DashboardTaskIds.GA_APPLICATION_ISSUED,
                2
            ),
            new ContributorSpec(
                new MakeDecisionDashboardTaskContributor(
                    mock(MakeDecisionApplicantDashboardTask.class),
                    mock(MakeDecisionRespondentDashboardTask.class)
                ),
                DashboardTaskIds.GA_MAKE_DECISION,
                2
            ),
            new ContributorSpec(
                new ApplicationSubmittedDashboardTaskContributor(
                    mock(ApplicationSubmittedApplicantDashboardTask.class),
                    mock(ApplicationSubmittedRespondentDashboardTask.class)
                ),
                DashboardTaskIds.GA_APPLICATION_SUBMITTED,
                2
            ),
            new ContributorSpec(
                new HwfOutcomeDashboardTaskContributor(
                    mock(HwfOutcomeApplicantDashboardTask.class)
                ),
                DashboardTaskIds.GA_HWF_OUTCOME,
                1
            )
        );

        for (ContributorSpec spec : specs) {
            DashboardTaskContribution contribution = spec.contribution();
            assertThat(contribution.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
            assertThat(contribution.taskId()).isEqualTo(spec.taskId());
            assertThat(contribution.dashboardTasks()).hasSize(spec.taskCount());
        }
    }

    private record ContributorSpec(DashboardTaskContribution contribution, String taskId, int taskCount) {
    }
}
