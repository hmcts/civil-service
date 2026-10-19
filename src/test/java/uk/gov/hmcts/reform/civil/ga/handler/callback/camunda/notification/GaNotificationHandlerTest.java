package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;

import java.util.HashMap;
import java.util.Map;

class GaNotificationHandlerTest {

    private static final String TASK_ID = "NotifyGaApplicantHwfOutcome";
    private static final Long CASE_ID = 1234567890123456L;
    private static final String SUMMARY = "Attempted: applicant@example.com : ref : template-a || Errors: ";

    private GaNotifierFactory gaNotifierFactory;
    private GaCoreCaseDataService gaCoreCaseDataService;
    private Notifier<GeneralApplicationCaseData> notifier;
    private GaNotificationHandler handler;
    private GeneralApplicationCaseData caseData;
    private CallbackParams params;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        gaNotifierFactory = mock(GaNotifierFactory.class);
        notifier = mock(Notifier.class);
        gaCoreCaseDataService = mock(GaCoreCaseDataService.class);
        handler = new GaNotificationHandler(gaNotifierFactory, gaCoreCaseDataService, new ObjectMapper());

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

        when(notifier.notifyParties(eq(caseData), eq(NOTIFY_EVENT.toString()), eq(TASK_ID)))
            .thenReturn(SUMMARY);

        handler.callbacks().get(ABOUT_TO_SUBMIT.getValue()).execute(params);

        verify(notifier).notifyParties(eq(caseData), eq(NOTIFY_EVENT.toString()), eq(TASK_ID));
        // kept on the case so the submitted callback can write it to the history event
        verify(caseData).setNotificationSummary(SUMMARY);
    }

    private StartEventResponse startEventFor(String summaryOnCase) {
        Map<String, Object> data = new HashMap<>();
        data.put("notificationSummary", summaryOnCase);
        return StartEventResponse.builder()
            .eventId(RECORD_NOTIFICATIONS.name())
            .token("token")
            .caseDetails(CaseDetails.builder().id(CASE_ID).data(data).build())
            .build();
    }

    private CaseDataContent recordAndCapture(String summary) {
        when(caseData.getNotificationSummary()).thenReturn(summary);
        when(gaCoreCaseDataService.startUpdate(String.valueOf(CASE_ID), RECORD_NOTIFICATIONS))
            .thenReturn(startEventFor(summary));

        handler.callbacks().get(SUBMITTED.getValue()).execute(params);

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(gaCoreCaseDataService).submitUpdate(eq(String.valueOf(CASE_ID)), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldRecordTheRecipientsAsTheHistoryEventSummary() {
        CaseDataContent content = recordAndCapture(
            "Attempted: applicant@example.com : ref : template-a || Errors: ");

        // AC2: the Record notification details entry shows who was notified and with which template
        assertThat(content.getEvent().getSummary())
            .isEqualTo("applicant@example.com : ref : template-a");
        assertThat(content.getEvent().getId()).isEqualTo(RECORD_NOTIFICATIONS.name());
    }

    @Test
    void shouldRecordErrorsAsTheHistoryEventDescription() {
        CaseDataContent content = recordAndCapture(
            "Attempted: applicant@example.com : ref : template-a || Errors: send failed");

        // AC3: errors go in the Comment field, and need not be user friendly
        assertThat(content.getEvent().getDescription()).isEqualTo("Errors: send failed");
    }

    @Test
    void shouldLeaveTheDescriptionUnsetWhenNothingFailed() {
        CaseDataContent content = recordAndCapture(
            "Attempted: applicant@example.com : ref : template-a || Errors: ");

        assertThat(content.getEvent().getDescription()).isNull();
    }

    @Test
    void shouldClearTheSummaryFromTheCaseSoItLivesOnlyOnTheHistoryEvent() {
        CaseDataContent content = recordAndCapture(
            "Attempted: applicant@example.com : ref : template-a || Errors: ");

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) content.getData();
        assertThat(data).containsEntry("notificationSummary", null);
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
