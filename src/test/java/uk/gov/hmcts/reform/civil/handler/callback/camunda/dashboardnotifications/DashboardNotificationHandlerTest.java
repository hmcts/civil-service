package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DashboardNotificationHandlerTest {

    private static final String TASK_ID = "generate-dashboard-task";

    @Mock
    private DashboardNotificationDispatcher dispatcher;
    @Captor
    private ArgumentCaptor<DashboardTaskContext> contextCaptor;

    @InjectMocks
    private DashboardNotificationHandler handler;

    private CallbackParams callbackParams;

    @BeforeEach
    void setup() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(123L)
            .businessProcess(new BusinessProcess().setActivityId(TASK_ID))
            .build();

        callbackParams = new CallbackParams()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .caseData(caseData);
    }

    @Test
    void shouldAdaptCallbackParamsAndDispatchToDashboardWorkflows() {
        handler.handle(callbackParams);

        verify(dispatcher).dispatch(eq(TASK_ID), contextCaptor.capture());
        assertThat(contextCaptor.getValue().caseType()).isEqualTo(DashboardCaseType.CIVIL);
        assertThat(contextCaptor.getValue().caseData()).isSameAs(callbackParams.getCaseData());
        assertThat(contextCaptor.getValue().callbackParams()).isSameAs(callbackParams);
    }
}
