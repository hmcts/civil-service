package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JudgementByAdmissionNonDivergentSpecDashboardTaskContributorTest {

    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardTask claimantTask;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndDashboardTasks() {
        JudgementByAdmissionNonDivergentSpecDashboardTaskContributor contributor =
            new JudgementByAdmissionNonDivergentSpecDashboardTaskContributor(
                claimantTask,
                defendantTask
            );

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC);
        assertThat(contributor.dashboardTasks()).containsExactly(
            claimantTask,
            defendantTask
        );
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(claimantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
