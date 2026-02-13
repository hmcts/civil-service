package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.citizenhearingfeepayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CitizenHearingFeePaymentDashboardTaskContributorTest {

    @Mock
    private CitizenHearingFeePaymentDashboardTask task;

    @Test
    void shouldExposeTaskIdAndHandler() {
        CitizenHearingFeePaymentDashboardTaskContributor contributor =
            new CitizenHearingFeePaymentDashboardTaskContributor(task);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CITIZEN_HEARING_FEE_PAYMENT);
        assertThat(contributor.dashboardTasks()).containsExactly(task);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(task))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
