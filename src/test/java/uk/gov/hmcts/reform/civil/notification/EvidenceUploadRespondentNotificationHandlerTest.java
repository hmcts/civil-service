package uk.gov.hmcts.reform.civil.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private EvidenceUploadRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
            when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(configuration.getLipContactEmail()).thenReturn("Email: contactocmc@justice.gov.uk");
            when(configuration.getLipContactEmailWelsh()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        }

        @Test
        void shouldNotifyRespondent1Solicitor_whenInvoked() {
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT);
            handler.notifyRespondentEvidenceUpload(caseData, true);

            verify(notificationService).sendMail(
                RESPONDENT1_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData, false, false),
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
                getNotificationDataMap(caseData, true, false),
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
                getNotificationDataMap(caseData, true, false),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent2Solicitor_whenInvoked() {
            //given: case data has two respondent solicitor
            when(notificationsProperties.getEvidenceUploadTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT).toBuilder()
                .respondent2(Party.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondentSolicitor2EmailAddress(RESPONDENT2_SOLICITOR_EMAIL)
                .build();
            //when: RepondentNotificationhandler for solictior2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent solicitor2
            verify(notificationService).sendMail(
                RESPONDENT2_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData, false, true),
                "evidence-upload-notification-" + caseData.getLegacyCaseReference()
            );
        }

        @Test
        void shouldNotifyRespondent2Lip_whenInvoked() {
            //given: case data has two respondents, with second being litigant in person
            when(notificationsProperties.getEvidenceUploadLipTemplate()).thenReturn(TEMPLATE_ID_LIP);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            CaseData caseData = createCaseDataWithText(NOTIFICATION_TEXT).toBuilder()
                .addRespondent2(YesOrNo.YES)
                .respondent2Represented(YesOrNo.NO)
                .respondent2(Party.builder()
                                 .individualLastName("Doe")
                                 .individualFirstName("John")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("Billy").partyEmail(RESPONDENT1_LIP_EMAIL).build())
                .build();
            //when: RepondentNotificationhandler for respondent 2 is called
            handler.notifyRespondentEvidenceUpload(caseData, false);
            //then: email should be sent to respondent 2
            verify(notificationService).sendMail(
                RESPONDENT1_LIP_EMAIL,
                TEMPLATE_ID_LIP,
                getNotificationDataMap(caseData, true, true),
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
            return CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .notificationText(notificationText)
                .build();
        }

        private CaseData createCaseDataForLip(String notificationText) {
            return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .notificationText(notificationText)
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("John")
                                 .individualLastName("Doe")
                                 .partyName("Billy").partyEmail(RESPONDENT1_LIP_EMAIL).build())
                .build();
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData, boolean isLip, boolean is1v2) {
            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, isLip ? caseData.getLegacyCaseReference() : caseData.getCcdCaseReference().toString(),
                UPLOADED_DOCUMENTS, caseData.getNotificationText(),
                PARTY_REFERENCES, is1v2
                    ? "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided"
                    : "Claimant reference: 12345 - Defendant reference: 6789",
                CLAIM_LEGAL_ORG_NAME_SPEC, isLip && !is1v2 ? "John Doe" : "org name",
                CASEMAN_REF, "000DC001"
            ));
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            return expectedProperties;
        }
    }
}
