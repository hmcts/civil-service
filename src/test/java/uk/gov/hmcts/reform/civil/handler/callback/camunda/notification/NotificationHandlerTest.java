package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
                .caseDetails(CaseDetailsBuilder.builder().data(caseData).build()).eventId("RECORD_NOTIFICATIONS").token(
                    "test").build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            handler.handle(params);

            verify(coreCaseDataService).startUpdate(any(), eq(RECORD_NOTIFICATIONS));
            verify(coreCaseDataService).submitUpdate(any(), any());
        }
    }


    @Nested
    class SubmittedCallback {

        @Test
        void shouldClearNotificationSummaryAfterRecording() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("Attempted: NotifierA, NotifierB || Errors: Error1")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", "Attempted: NotifierA, NotifierB || Errors: Error1");
            dataMap.put("ccdCaseReference", caseData.getCcdCaseReference());

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // CRITICAL ASSERTION: notificationSummary must be null
            @SuppressWarnings("unchecked")
            Map<String, Object> submittedData = (Map<String, Object>) submittedContent.getData();
            assertThat(submittedData.get("notificationSummary")).isNull();
        }

        @Test
        void shouldBuildEventWithSummaryOnly() {
            // Given - notification summary without errors
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("Attempted: NotifierA, NotifierB")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", "Attempted: NotifierA, NotifierB");

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // Event should have summary but no description
            assertThat(submittedContent.getEvent().getSummary()).isEqualTo("NotifierA, NotifierB");
            assertThat(submittedContent.getEvent().getDescription()).isNull();
        }

        @Test
        void shouldBuildEventWithSummaryAndErrors() {
            // Given - notification summary with errors
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("Attempted: NotifierA || Errors: Failed to send email")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", "Attempted: NotifierA || Errors: Failed to send email");

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // Event should have summary and error description
            assertThat(submittedContent.getEvent().getSummary()).isEqualTo("NotifierA");
            assertThat(submittedContent.getEvent().getDescription()).isEqualTo("Errors: Failed to send email");
        }

        @Test
        void shouldHandleNullNotificationSummary() {
            // Given - null notification summary
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary(null)
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", null);

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // Should handle null gracefully
            assertThat(submittedContent.getEvent().getSummary()).isNull();
            assertThat(submittedContent.getEvent().getDescription()).isNull();
        }

        @Test
        void shouldHandleSummaryWithoutAttemptedPrefix() {
            // Given - summary without "Attempted: " prefix
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("NotifierA, NotifierB")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", "NotifierA, NotifierB");

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // Should use the summary as-is without prefix
            assertThat(submittedContent.getEvent().getSummary()).isEqualTo("NotifierA, NotifierB");
        }

        @Test
        void shouldHandleComplexNotificationSummaryWithMultipleErrors() {
            // Given - complex summary with multiple notifiers and errors
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("Attempted: NotifierA, NotifierB, NotifierC || Errors: Error1, Error2, Error3")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(
                "notificationSummary",
                "Attempted: NotifierA, NotifierB, NotifierC || Errors: Error1, Error2, Error3"
            );

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            // When
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            // Then
            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            // Should correctly parse complex summary
            assertThat(submittedContent.getEvent().getSummary()).isEqualTo("NotifierA, NotifierB, NotifierC");
            assertThat(submittedContent.getEvent().getDescription()).isEqualTo("Errors: Error1, Error2, Error3");
            @SuppressWarnings("unchecked")
            Map<String, Object> submittedData = (Map<String, Object>) submittedContent.getData();
            assertThat(submittedData.get("notificationSummary")).isNull();
        }

        @Test
        void shouldHandleEmptyErrorsSection() {
            // Given - errors delimiter present but errors section is empty/blank
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .businessProcess(BusinessProcess.builder().activityId(TASK_ID).build())
                .notificationSummary("Attempted: NotifierA || Errors: ")
                .build();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("notificationSummary", "Attempted: NotifierA || Errors: ");

            CaseDetails caseDetails = CaseDetailsBuilder.builder()
                .data(caseData)
                .build();
            caseDetails.setData(dataMap);

            StartEventResponse response = StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("RECORD_NOTIFICATIONS")
                .token("test")
                .build();

            when(coreCaseDataService.startUpdate(caseData.getCcdCaseReference().toString(), RECORD_NOTIFICATIONS))
                .thenReturn(response);

            ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            handler.handle(params);

            verify(coreCaseDataService).submitUpdate(any(), contentCaptor.capture());
            CaseDataContent submittedContent = contentCaptor.getValue();

            assertThat(submittedContent.getEvent().getSummary()).isEqualTo("NotifierA");
            assertThat(submittedContent.getEvent().getDescription()).isNull();
        }
    }
}
