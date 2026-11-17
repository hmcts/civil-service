package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationDispatcher;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationRegistry;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class RequestForReconsiderationDashboardTaskContributorTest {

    @Test
    void shouldDispatchOnlyClaimantTaskForDefendantRequest() {
        RequestForReconsiderationClaimantDashboardTask task = mock(RequestForReconsiderationClaimantDashboardTask.class);
        DashboardTaskContributor contributor = new RequestForReconsiderationClaimantDashboardTaskContributor(task);
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributor));

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT);
        assertThat(contributor.caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT, DashboardCaseType.CIVIL)).containsExactly(task);
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT, DashboardCaseType.GENERAL_APPLICATION)).isEmpty();
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT,
            DashboardCaseType.CIVIL)).isEmpty();
        DashboardTaskContext context = DashboardTaskContext.civil(CaseData.builder().build(), "token");

        new DashboardNotificationDispatcher(registry).dispatch(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT, context);

        verify(task).execute(context);
        verifyNoMoreInteractions(task);
    }

    @Test
    void shouldDispatchOnlyDefendantTaskForClaimantRequest() {
        RequestForReconsiderationDefendantDashboardTask task = mock(RequestForReconsiderationDefendantDashboardTask.class);
        DashboardTaskContributor contributor = new RequestForReconsiderationDefendantDashboardTaskContributor(task);
        DashboardNotificationRegistry registry = new DashboardNotificationRegistry(List.of(contributor));

        assertThat(contributor.taskId()).isEqualTo(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT);
        assertThat(contributor.caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT, DashboardCaseType.CIVIL)).containsExactly(task);
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT, DashboardCaseType.GENERAL_APPLICATION)).isEmpty();
        assertThat(registry.workflowsFor(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_CLAIMANT,
            DashboardCaseType.CIVIL)).isEmpty();
        DashboardTaskContext context = DashboardTaskContext.civil(CaseData.builder().build(), "token");

        new DashboardNotificationDispatcher(registry).dispatch(DashboardTaskIds.REQUEST_FOR_RECONSIDERATION_DEFENDANT, context);

        verify(task).execute(context);
        verifyNoMoreInteractions(task);
    }
}
