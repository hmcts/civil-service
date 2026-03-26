package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantsignsettlementagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DefendantSignSettlementAgreementDashboardTaskContributorTest {

    @Mock
    private DefendantSignSettlementAgreementDefendantDashboardTask defendantTask;

    @Test
    void shouldExposeTaskIdAndHandlers() {
        var contributor = new DefendantSignSettlementAgreementDashboardTaskContributor(defendantTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT);
        assertThat(contributor.dashboardTasks()).containsExactly(defendantTask);
        assertThatThrownBy(() -> contributor.dashboardTasks().add(defendantTask))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
