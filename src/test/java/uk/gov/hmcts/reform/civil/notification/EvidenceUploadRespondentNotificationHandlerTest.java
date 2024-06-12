package uk.gov.hmcts.reform.civil.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn("template-id-lip");
            when(notificationsProperties.getEvidenceUploadLipTemplateWelsh()).thenReturn("template-id-lip-welsh");
        }

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            //given: case data has one respondent solicitor
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .build();;
            //when: RepondentNotificationhandler for solictior1 is called
            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent solicitor1
            verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    "template-id",
                    getNotificationDataMap(caseData),
                    "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1Lip_whenInvoked() {
            //given: case data has one respondent litigant in person
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder().partyName("Billy").partyEmail("respondent@example.com").build())
                .build();
            //when: RepondentNotificationhandler for respondent 1 is called

            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-lip",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1LipinWelsh_whenInvoked() {
            //given: case data has one respondent litigant in person
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build())
                                 .build())
                .build().toBuilder()
                .notificationText("example of uploaded documents")
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder().partyName("Billy").partyEmail("respondent@example.com").build())
                .build();
            //when: RepondentNotificationhandler for respondent 1 is called

            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-lip-welsh",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
            //given: case data has two respondent solicitor
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .addRespondent2(YesOrNo.YES)
                .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com")
                .build();
            //when: RepondentNotificationhandler for solictior2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent solicitor2
            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Lip_whenInvoked() {
            //given: case data has two respondents, with second being litigant in person
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("example of uploaded documents")
                .addRespondent2(YesOrNo.YES)
                .respondent2Represented(YesOrNo.NO)
                .respondent2(Party.builder().partyName("Billy").partyEmail("respondent@example.com").build())
                .build();
            //when: RepondentNotificationhandler for respondent 2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent 2
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-lip",
                getNotificationDataMap(caseData),
                "evidence-upload-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondent1Solicitor_whenInvokedAndNoNotificationContent() {
            //Once emails are sent, we want to null notificationText, so any future emails will not contain past content.
            //unable to null directly in EvidenceUploadNotificationEventHandler, so assigned as NULLED.
            //given: case data has one respondent solicitor
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText("NULLED")
                .build();
            //when: RepondentNotificationhandler for solictior1 is called
            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent solicitor1
            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyRespondent1Solicitor_whenInvokedAndNoContentNull() {
            //given: case data has one respondent solicitor
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(null)
                .build();
            //when: RepondentNotificationhandler for solictior1 is called
            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent solicitor1
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
