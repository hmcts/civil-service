package uk.gov.hmcts.reform.civil.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.UPLOADED_DOCUMENTS;

@SpringBootTest(classes = {
    EvidenceUploadApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class EvidenceUploadApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private EvidenceUploadApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn("template-id");
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn("template-id-lip");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            //given: case where applicant solicitor has email as applicantsolicitor@example.com
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .build();
            //when: ApplicantNotificationhandler is called
            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant solicitor
            verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            //given: case where applicant litigant in person has email as applicant@example.com
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyName("Billy").partyEmail("applicant@example.com").build())
                .build();
            //when: ApplicantNotificationhandler is called
            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant
            verify(notificationService).sendMail(
                "applicant@example.com",
                "template-id-lip",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyApplicantLip_whenInvokedAndNoNotificationContent() {
            //Once emails are sent, we want to null notificationText, so any future emails will not contain past content.
            //unable to null directly in EvidenceUploadNotificationEventHandler, so assigned as NULLED.
            //given: case where applicant litigant in person has email as applicant@example.com
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().builder()
                .notificationText("NULLED")
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyName("Billy").partyEmail("applicant@example.com").build())
                .build();
            //when: ApplicantNotificationhandler is called
            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant
            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyApplicantLip_whenInvokedAndNoNotificationContentNull() {
            //given: case where applicant litigant in person has email as applicant@example.com
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(null)
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyName("Billy").partyEmail("applicant@example.com").build())
                .build();
            //when: ApplicantNotificationhandler is called
            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant
            verifyNoInteractions(notificationService);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                    CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                    UPLOADED_DOCUMENTS, "example of uploaded documents"
            );
        }
    }
}
