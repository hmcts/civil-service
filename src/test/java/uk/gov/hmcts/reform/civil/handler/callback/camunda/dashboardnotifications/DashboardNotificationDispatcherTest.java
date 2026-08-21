package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationDispatcherTest {

    private static final String TASK_ID = "generate-dashboard-task";

    @Mock
    private DashboardNotificationRegistry registry;

    @Mock
    private DashboardWorkflowTask handlerOne;

    @Mock
    private DashboardWorkflowTask handlerTwo;

    @InjectMocks
    private DashboardNotificationDispatcher dispatcher;

    @Test
    void shouldDispatchToRegisteredHandlers() {
        DashboardTaskContext context = DashboardTaskContext.civil(CaseData.builder().build(), "token");
        when(registry.workflowsFor(TASK_ID, DashboardCaseType.CIVIL)).thenReturn(List.of(handlerOne, handlerTwo));

        dispatcher.dispatch(TASK_ID, context);

        verify(handlerOne).execute(context);
        verify(handlerTwo).execute(context);
    }

    @Test
    void shouldNotFailWhenNoHandlersRegistered() {
        DashboardTaskContext context = DashboardTaskContext.civil(CaseData.builder().build(), "token");
        when(registry.workflowsFor(TASK_ID, DashboardCaseType.CIVIL)).thenReturn(List.of());

        dispatcher.dispatch(TASK_ID, context);

        verify(registry).workflowsFor(TASK_ID, DashboardCaseType.CIVIL);
        verifyNoInteractions(handlerOne, handlerTwo);
    }
}
