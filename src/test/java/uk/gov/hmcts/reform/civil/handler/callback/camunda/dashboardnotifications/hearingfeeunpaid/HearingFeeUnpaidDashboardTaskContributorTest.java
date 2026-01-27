package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingfeeunpaid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingFeeUnpaidDashboardTaskContributorTest {

    @Mock
    private HearingFeeUnpaidClaimantDashboardTask claimantTask;
    @Mock
    private HearingFeeUnpaidDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        HearingFeeUnpaidDashboardTaskContributor taskContributor =
            new HearingFeeUnpaidDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(taskContributor.taskId()).isEqualTo(DashboardTaskIds.HEARING_FEE_UNPAID);
        assertThat(taskContributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
        assertThatThrownBy(() -> taskContributor.dashboardTasks().add(claimantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
