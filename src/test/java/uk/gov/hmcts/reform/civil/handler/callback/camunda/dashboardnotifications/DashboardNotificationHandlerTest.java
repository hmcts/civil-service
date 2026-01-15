package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationHandlerTest {

    private static final String TASK_ID = "generate-dashboard-task";

    @Mock
    private DashboardNotificationRegistry registry;

    @Mock
    private DashboardWorkflowTask handlerOne;

    @Mock
    private DashboardWorkflowTask handlerTwo;

    @InjectMocks
    private DashboardNotificationHandler handler;

    private CallbackParams callbackParams;

    @BeforeEach
    void setup() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(123L)
            .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData)
            .build();
    }

    @Test
    void shouldDispatchToRegisteredHandlers() {
        when(registry.workflowsFor(TASK_ID)).thenReturn(List.of(handlerOne, handlerTwo));

        handler.handle(callbackParams);

        verify(handlerOne).execute(any(DashboardTaskContext.class));
        verify(handlerTwo).execute(any(DashboardTaskContext.class));
    }

    @Test
    void shouldNotFailWhenNoHandlersRegistered() {
        when(registry.workflowsFor(TASK_ID)).thenReturn(List.of());

        handler.handle(callbackParams);

        verify(registry).workflowsFor(TASK_ID);
        verifyNoInteractions(handlerOne, handlerTwo);
    }
}
