package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CaseTakenOfflineRespondentNotificationHandler.TASK_ID_RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CaseTakenOfflineRespondentNotificationHandler.TASK_ID_RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitorOne_whenInvoked() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "case-taken-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvoked() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "case-taken-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvokedWithSameSol() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentSolicitor2EmailAddress(null).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "case-taken-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitorTwo_whenInvokedWithDiffSol() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor2EmailAddress(null).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                null,
                "template-id",
                getNotificationDataMap(caseData),
                "case-taken-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_ONE);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT_TWO);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE);
            assertThat(handler.handledEvents()).contains(NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE);
        }
    }
}
