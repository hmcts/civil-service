package uk.gov.hmcts.reform.civil.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.UPLOADED_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {


    private static final String RESPONDENT1_SOLICITOR_EMAIL = "respondentsolicitor@example.com";
    private static final String RESPONDENT1_LIP_EMAIL = "respondent@example.com";
    private static final String RESPONDENT2_SOLICITOR_EMAIL = "respondentsolicitor2@example.com";
    private static final String NOTIFICATION_TEXT = "example of uploaded documents";
    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_LIP = "template-id-lip";
    private static final String TEMPLATE_ID_WELSH_LIP = "template-id-lip-welsh";
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private EvidenceUploadRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn(TEMPLATE_ID);
            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT);
            handler.notifyRespondentEvidenceUpload(caseData, true);

            verify(notificationService).sendMail(
                RESPONDENT1_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent1Lip_whenInvoked() {
            //given: case data has one respondent litigant in person
            //when: RepondentNotificationhandler for respondent 1 is called
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn(TEMPLATE_ID_LIP);
            CaseData caseData = createCaseDataForLip(NOTIFICATION_TEXT);

            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent
            verify(notificationService).sendMail(
                RESPONDENT1_LIP_EMAIL,
                TEMPLATE_ID_LIP,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent1LipInWelsh_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadLipTemplateWelsh()).thenReturn(TEMPLATE_ID_WELSH_LIP);
            CaseData caseData = createCaseDataForLip(NOTIFICATION_TEXT).toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build())
                                 .build())
                .build();
            //when: RepondentNotificationhandler for respondent 1 is called

            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent
            verify(notificationService).sendMail(
                RESPONDENT1_LIP_EMAIL,
                TEMPLATE_ID_WELSH_LIP,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
            //given: case data has two respondent solicitor
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn(TEMPLATE_ID);
            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT).toBuilder()
                .addRespondent2(YesOrNo.YES)
                .respondentSolicitor2EmailAddress(RESPONDENT2_SOLICITOR_EMAIL)
                .build();
            //when: RepondentNotificationhandler for solictior2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent solicitor2
            verify(notificationService).sendMail(
                RESPONDENT2_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent2Lip_whenInvoked() {
            //given: case data has two respondents, with second being litigant in person
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn(TEMPLATE_ID_LIP);

            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT).toBuilder()
                .addRespondent2(YesOrNo.YES)
                .respondent2Represented(YesOrNo.NO)
                .respondent2(Party.builder().partyName("Billy").partyEmail(RESPONDENT1_LIP_EMAIL).build())
                .build();
            //when: RepondentNotificationhandler for respondent 2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent 2
            verify(notificationService).sendMail(
                RESPONDENT1_LIP_EMAIL,
                TEMPLATE_ID_LIP,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotNotifyRespondent1Solicitor_whenInvokedAndNoNotificationContent() {
            CaseData caseData = createCaseDataWithText("NULLED");
            //when: RepondentNotificationhandler for solicitor1 is called
            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent solicitor1
            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyRespondent1Solicitor_whenInvokedAndNoNotificiationContentNull() {
            //given: case data has one respondent solicitor
            CaseData caseData = createCaseDataWithText(null);
            //when: RepondentNotificationhandler for solictior1 is called
            handler.notifyRespondentEvidenceUpload(caseData, true);
            //then: email should be sent to respondent solicitor1
            verifyNoInteractions(notificationService);
        }

        private CaseData createCaseDataWithText(String notificationText) {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(notificationText)
                .build();
        }
        private CaseData createCaseDataForLip(String notificationText) {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(notificationText)
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder().partyName("Billy").partyEmail(RESPONDENT1_LIP_EMAIL).build())
                .build();
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                UPLOADED_DOCUMENTS, caseData.getNotificationText()
            );
        }
    }
}
