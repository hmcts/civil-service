package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefendantResponseCuiDashboardTaskContributorTest {

    @Mock
    private DefendantResponseCuiClaimantDashboardTask claimantTask;
    @Mock
    private DefendantResponseDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        DefendantResponseCuiDashboardTaskContributor contributor =
            new DefendantResponseCuiDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DEFENDANT_RESPONSE_CUI);
        assertThat(contributor.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
