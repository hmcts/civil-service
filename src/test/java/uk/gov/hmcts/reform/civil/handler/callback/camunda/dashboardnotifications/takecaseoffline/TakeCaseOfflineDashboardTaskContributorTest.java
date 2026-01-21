package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineDashboardTaskContributorTest {

    @Mock
    private CaseProceedOfflineClaimantDashboardTask claimantCaseProceedOfflineTask;
    @Mock
    private CaseProceedOfflineDefendantDashboardTask defendantCaseProceedOfflineTask;
    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantApplicationOfflineTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantApplicationOfflineTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        TakeCaseOfflineDashboardTaskContributor contributor =
            new TakeCaseOfflineDashboardTaskContributor(
                claimantCaseProceedOfflineTask,
                defendantCaseProceedOfflineTask,
                claimantApplicationOfflineTask,
                defendantApplicationOfflineTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.TAKE_CASE_OFFLINE);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantCaseProceedOfflineTask,
            defendantCaseProceedOfflineTask,
            claimantApplicationOfflineTask,
            defendantApplicationOfflineTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantCaseProceedOfflineTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
