package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantnoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ApplyNocDecisionDefendantLipDashboardTaskContributorTest {

    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantTask;
    @Mock
    private DefendantNocClaimantDashboardTask offlineTask;
    @Mock
    private ClaimantNocOnlineDashboardTask onlineTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        ApplyNocDecisionDefendantLipDashboardTaskContributor contributor =
            new ApplyNocDecisionDefendantLipDashboardTaskContributor(
                claimantTask,
                defendantTask,
                offlineTask,
                onlineTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.APPLY_NOC_DECISION_DEFENDANT_LIP);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            offlineTask,
            onlineTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
