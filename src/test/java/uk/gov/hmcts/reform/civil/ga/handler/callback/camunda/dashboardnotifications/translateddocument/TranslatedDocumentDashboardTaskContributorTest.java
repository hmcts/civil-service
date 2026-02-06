package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.translateddocument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardWorkflowTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TranslatedDocumentDashboardTaskContributorTest {

    @Mock
    private TranslatedDocumentApplicantDashboardTask applicantTask;
    @Mock
    private TranslatedDocumentRespondentDashboardTask respondentTask;

    @Test
    void shouldExposeTaskIdCaseTypeAndDashboardTasks() {
        TranslatedDocumentDashboardTaskContributor contributor =
            new TranslatedDocumentDashboardTaskContributor(applicantTask, respondentTask);

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.GA_TRANSLATED_DOCUMENT);
        assertThat(contributor.caseType()).isEqualTo(DashboardCaseType.GENERAL_APPLICATION);
        assertThat(contributor.dashboardTasks()).containsExactly(applicantTask, respondentTask);
        assertListIsImmutable(contributor.dashboardTasks());
    }

    private void assertListIsImmutable(java.util.List<DashboardWorkflowTask> tasks) {
        assertThatThrownBy(() -> tasks.add(applicantTask)).isInstanceOf(UnsupportedOperationException.class);
    }
}
