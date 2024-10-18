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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static org.mockito.Mockito.verify;

import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
    private static final String APPLICANT_LIP_EMAIL = "applicant@example.com";
    private static final String NOTIFICATION_TEXT = "example of uploaded documents";
    private static final String TEMPLATE_ID = "template-id";
    private static final String TEMPLATE_ID_LIP = "template-id-lip";
    private static final String TEMPLATE_ID_WELSH_LIP = "template-id-welsh-lip";
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private EvidenceUploadApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn(TEMPLATE_ID);

            CaseData caseData = createCaseDataWithText();

            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant solicitor
            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn(TEMPLATE_ID_LIP);

            CaseData caseData = createCaseDataForLip(NOTIFICATION_TEXT);

            handler.notifyApplicantEvidenceUpload(caseData);
            //then: email should be sent to applicant
            verify(notificationService).sendMail(
                APPLICANT_LIP_EMAIL,
                TEMPLATE_ID_LIP,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyApplicantLipInWelsh_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadLipTemplateWelsh()).thenReturn(TEMPLATE_ID_WELSH_LIP);

            CaseData caseData = createCaseDataForLip(NOTIFICATION_TEXT).toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();
            handler.notifyApplicantEvidenceUpload(caseData);

            verify(notificationService).sendMail(
                APPLICANT_LIP_EMAIL,
                TEMPLATE_ID_WELSH_LIP,
                getNotificationDataMap(caseData),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotNotifyApplicantLip_whenInvokedAndNoNotificationContent() {
            CaseData caseData = createCaseDataForLip("NULLED");

            handler.notifyApplicantEvidenceUpload(caseData);
            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyApplicantLip_whenInvokedAndNoNotificationContentNull() {
            CaseData caseData = createCaseDataForLip(null);

            handler.notifyApplicantEvidenceUpload(caseData);
            verifyNoInteractions(notificationService);
        }

        private CaseData createCaseDataWithText() {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(EvidenceUploadApplicantNotificationHandlerTest.NOTIFICATION_TEXT)
                .build();
        }

        private CaseData createCaseDataForLip(String notificationText) {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(notificationText)
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("Billy").partyEmail(APPLICANT_LIP_EMAIL).build())
                .build();
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, YesOrNo.NO.equals(caseData.getApplicant1Represented())
                    ? caseData.getLegacyCaseReference() : caseData.getCcdCaseReference().toString(),
                UPLOADED_DOCUMENTS, caseData.getNotificationText(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIM_LEGAL_ORG_NAME_SPEC, YesOrNo.NO.equals(caseData.getApplicant1Represented())
                    ? "John Doe" : "Signer Name"
            );
        }
    }
}
