package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

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
    ObjectMapper objectMapper;

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

        @Test
        void shouldRecordNotifications() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().businessProcess(
                BusinessProcess.builder().activityId(TASK_ID).build()
            ).notificationSummary("Summary of notifications").build();

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("RECORD_NOTIFICATIONS").token("test").build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            handler.handle(params);

            verify(coreCaseDataService).startUpdate(any(), eq(RECORD_NOTIFICATIONS));
            verify(coreCaseDataService).submitUpdate(any(), any());
        }
    }
}
