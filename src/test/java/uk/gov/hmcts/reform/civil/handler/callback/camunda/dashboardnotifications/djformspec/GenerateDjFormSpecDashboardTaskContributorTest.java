package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.djformspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GenerateDjFormSpecDashboardTaskContributorTest {

    @Mock
    private ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask;
    @Mock
    private CcjRequestedClaimantDashboardTask claimantCcjTask;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask;
    @Mock
    private CcjRequestedDefendantDashboardTask defendantCcjTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        GenerateDjFormSpecDashboardTaskContributor contributor =
            new GenerateDjFormSpecDashboardTaskContributor(
                claimantOfflineTask,
                claimantCcjTask,
                defendantOfflineTask,
                defendantCcjTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.GENERATE_DJ_FORM_SPEC);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantOfflineTask,
            claimantCcjTask,
            defendantOfflineTask,
            defendantCcjTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantOfflineTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
