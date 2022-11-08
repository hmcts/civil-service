package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.EvidenceUploadRespondentNotificationHandler.TASK_ID_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.EvidenceUploadRespondentNotificationHandler.TASK_ID_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@SpringBootTest(classes = {
        EvidenceUploadRespondentNotificationHandler.class,
        JacksonAutoConfiguration.class
})
class EvidenceUploadRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private EvidenceUploadRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            //given: case data has one respondent solicitor
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_EVIDENCE_UPLOAD.name())
                             .build()).build();

            //when: about to submit is called for callback handler with respondent1 event
            handler.handle(params);

            //then: email should be sent to respondent solicitor1
            verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
            //given: case data has two respondent solicitor
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified().addRespondent2(YesOrNo.YES)
                .respondentSolicitor2EmailAddress("respondentsolicitor@example.com")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_EVIDENCE_UPLOAD.name()).build()).build();

            //when: about to submit is called for callback handler with respondent2 event
            handler.handle(params);

            //then: email should be sent to respondent solicitor2
            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                     .request(CallbackRequest.builder().eventId(
                                                         NOTIFY_RESPONDENT_SOLICITOR1_FOR_EVIDENCE_UPLOAD.name())
                                                                  .build()).build())).isEqualTo(TASK_ID_RESPONDENT1);
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                     .request(CallbackRequest.builder().eventId(
                                                         NOTIFY_RESPONDENT_SOLICITOR2_FOR_EVIDENCE_UPLOAD.name())
                                                                  .build()).build())).isEqualTo(
                TASK_ID_RESPONDENT2);
        }

        @Test
        void shouldReturnCorrectEvent_whenInvoked() {
            assertThat(handler.handledEvents()).isEqualTo(List.of(NOTIFY_RESPONDENT_SOLICITOR1_FOR_EVIDENCE_UPLOAD,
                                                                  NOTIFY_RESPONDENT_SOLICITOR2_FOR_EVIDENCE_UPLOAD));
        }
    }
}
