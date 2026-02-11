package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingScheduledDashboardTaskContributorTest {

    @Mock
    private HearingScheduledClaimantDashboardTask claimantTask;
    @Mock
    private HearingScheduledDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlersForStandard() {
        HearingScheduledDashboardTaskContributor contributor =
            new HearingScheduledDashboardTaskContributor(DashboardTaskIds.HEARING_SCHEDULED, claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.HEARING_SCHEDULED);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }

    @Test
    void shouldExposeTaskIdAndHandlersForHmc() {
        HearingScheduledDashboardTaskContributor contributor =
            new HearingScheduledDashboardTaskContributor(DashboardTaskIds.HEARING_SCHEDULED_HMC, claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.HEARING_SCHEDULED_HMC);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
