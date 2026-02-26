package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.helpwithhearingfees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpWithHearingFeesDashboardTaskContributorTest {

    @Mock
    private HelpWithHearingFeesClaimantDashboardTask claimantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        HelpWithHearingFeesDashboardTaskContributor contributor =
            new HelpWithHearingFeesDashboardTaskContributor(claimantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.HELP_WITH_HEARING_FEES);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
