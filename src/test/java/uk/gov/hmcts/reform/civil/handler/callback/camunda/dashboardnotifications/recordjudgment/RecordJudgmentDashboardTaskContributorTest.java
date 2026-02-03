package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.recordjudgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordJudgmentDashboardTaskContributorTest {

    @Mock
    private RecordJudgmentDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        RecordJudgmentDashboardTaskContributor contributor =
            new RecordJudgmentDashboardTaskContributor(defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.RECORD_JUDGMENT);
        assertThat(contributor.dashboardTasks()).containsExactly(defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(defendantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
