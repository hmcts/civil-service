package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmittedDashboardTaskContributorTest {

    @Mock
    private ApplicationSubmittedApplicantDashboardTask applicantTask;
    @Mock
    private ApplicationSubmittedRespondentDashboardTask respondentTask;

    @Test
    void shouldExposeTaskIdCaseTypeAndDashboardTasks() {
        ApplicationSubmittedDashboardTaskContributor contributor =
            new ApplicationSubmittedDashboardTaskContributor(applicantTask, respondentTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.GA_APPLICATION_SUBMITTED);
        assertThat(contributor.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
        assertThat(contributor.dashboardTasks()).containsExactly(applicantTask, respondentTask);
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(applicantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
