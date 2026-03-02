package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentDashboardTaskContributorTest {

    @Mock
    private CreateClaimAfterPaymentApplicantDashboardTask applicantTask;
    @Mock
    private CreateClaimAfterPaymentDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        CreateClaimAfterPaymentDashboardTaskContributor contributor =
            new CreateClaimAfterPaymentDashboardTaskContributor(defendantTask, applicantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.CREATE_CLAIM_AFTER_PAYMENT);
        assertThat(contributor.dashboardTasks()).containsExactly(defendantTask, applicantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(applicantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
