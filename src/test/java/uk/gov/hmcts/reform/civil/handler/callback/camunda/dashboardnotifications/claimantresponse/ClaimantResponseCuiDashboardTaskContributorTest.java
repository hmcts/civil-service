package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

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
class ClaimantResponseCuiDashboardTaskContributorTest {

    @Mock
    private ClaimantResponseClaimantDashboardTask claimantTask;
    @Mock
    private ClaimantResponseDefendantDashboardTask defendantTask;
    @Mock
    private ClaimantCcjResponseClaimantDashboardTask claimantCcjTask;
    @Mock
    private ClaimantCcjResponseDefendantDashboardTask defendantCcjTask;
    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        ClaimantResponseCuiDashboardTaskContributor contributor = new ClaimantResponseCuiDashboardTaskContributor(
            claimantTask,
            defendantTask,
            claimantCcjTask,
            defendantCcjTask,
            claimantOfflineTask,
            defendantOfflineTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CLAIMANT_RESPONSE_CUI);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask,
            claimantOfflineTask,
            defendantOfflineTask,
            claimantCcjTask,
            defendantCcjTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
