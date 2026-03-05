package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.informagreedextensiondatespec;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContribution;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InformAgreedExtensionDateSpecDashboardTaskContributorTest {

    @Test
    void shouldExposeTaskIdAndTasks() {
        InformAgreedExtensionDateSpecClaimantDashboardTask claimantTask =
            mock(InformAgreedExtensionDateSpecClaimantDashboardTask.class);
        InformAgreedExtensionDateSpecDefendantDashboardTask defendantTask =
            mock(InformAgreedExtensionDateSpecDefendantDashboardTask.class);

        DashboardTaskContribution contribution =
            new InformAgreedExtensionDateSpecDashboardTaskContributor(claimantTask, defendantTask);

        assertThat(contribution.taskId()).isEqualTo(DashboardTaskIds.INFORM_AGREED_EXTENSION_DATE_SPEC);
        assertThat(contribution.dashboardTasks()).containsExactly(claimantTask, defendantTask);
    }
}
