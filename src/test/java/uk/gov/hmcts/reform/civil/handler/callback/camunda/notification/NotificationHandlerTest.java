package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String TASK_ID = "taskId";
    @Mock
    NotifierFactory notifierFactory;

    @Mock
    Notifier notifier;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @Mock
    CaseTaskTrackingService caseTaskTrackingService;

    @InjectMocks
    private NotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyParties() {
            when(notifierFactory.getNotifier(TASK_ID)).thenReturn(notifier);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().businessProcess(
                BusinessProcess.builder().activityId(TASK_ID).build()
            ).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notifier).notifyParties(caseData, NOTIFY_EVENT.toString(), TASK_ID);
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldCreateRecordNotificationsEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().businessProcess(
                BusinessProcess.builder().activityId(TASK_ID).build()
            ).build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
            StartEventResponse start = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("token")
                .build();

            when(coreCaseDataService.startUpdate(eq(caseData.getCcdCaseReference().toString()), any())).thenReturn(start);
            when(caseTaskTrackingService.consumeErrors(caseData.getCcdCaseReference().toString(), TASK_ID))
                .thenReturn("Failed to send email");

            CallbackParams params = CallbackParamsBuilder.builder().of(uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED, caseData).build();
            handler.handle(params);

            verify(coreCaseDataService).startUpdate(eq(caseData.getCcdCaseReference().toString()), any());
            ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);
            verify(coreCaseDataService).submitUpdate(eq(caseData.getCcdCaseReference().toString()), captor.capture());
            CaseDataContent submitted = captor.getValue();
            // summary equals activity id; description contains raw error text (no prefix)
            org.assertj.core.api.Assertions.assertThat(submitted.getEvent().getSummary()).isEqualTo(TASK_ID);
            org.assertj.core.api.Assertions.assertThat(submitted.getEvent().getDescription()).isEqualTo("Failed to send email");
        }
    }
}
