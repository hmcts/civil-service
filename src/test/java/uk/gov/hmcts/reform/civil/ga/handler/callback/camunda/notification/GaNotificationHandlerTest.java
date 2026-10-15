package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;

class GaNotificationHandlerTest {

    private static final String TASK_ID = "NotifyGaApplicantHwfOutcome";
    private static final Long CASE_ID = 1234567890123456L;

    private GaNotifierFactory gaNotifierFactory;
    private Notifier<GeneralApplicationCaseData> notifier;
    private GaNotificationHandler handler;
    private GeneralApplicationCaseData caseData;
    private CallbackParams params;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        gaNotifierFactory = mock(GaNotifierFactory.class);
        notifier = mock(Notifier.class);
        handler = new GaNotificationHandler(gaNotifierFactory);

        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setActivityId(TASK_ID);

        caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getBusinessProcess()).thenReturn(businessProcess);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);

        params = mock(CallbackParams.class);
        when(params.getGeneralApplicationCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldHandleNotifyEventOnly() {
        assertThat(handler.handledEvents()).containsExactly(NOTIFY_EVENT);
    }

    @Test
    void shouldResolveTheTaskIdFromTheGeneralApplicationBusinessProcess() {
        assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID);
    }

    @Test
    void shouldDispatchToTheNotifierRegisteredForTheActivityId() {
        when(gaNotifierFactory.getNotifier(TASK_ID)).thenReturn(notifier);

        handler.callbacks().get(ABOUT_TO_SUBMIT.getValue()).execute(params);

        verify(notifier).notifyParties(eq(caseData), eq(NOTIFY_EVENT.toString()), eq(TASK_ID));
    }

    @Test
    void shouldFailLoudlyWhenNoNotifierIsRegistered() {
        when(gaNotifierFactory.getNotifier(TASK_ID)).thenReturn(null);

        assertThatThrownBy(() -> handler.callbacks().get(ABOUT_TO_SUBMIT.getValue()).execute(params))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No GA Notifier registered for taskId '" + TASK_ID + "'")
            .hasMessageContaining(String.valueOf(CASE_ID));
    }
}
