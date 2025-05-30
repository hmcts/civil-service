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
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.GenerateOrderNotificationHandler.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.GenerateOrderNotificationHandler.TASK_ID_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.GenerateOrderNotificationHandler.TASK_ID_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
public class GenerateOrderNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private GenerateOrderNotificationHandler handler;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getGenerateOrderNotificationTemplate()).thenReturn("template-id");
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvoked() {
            when(notificationsProperties.getGenerateOrderNotificationTemplate()).thenReturn("template-id");
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked() {
            when(notificationsProperties.getGenerateOrderNotificationTemplate()).thenReturn("template-id");
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER.name()).build()
            ).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1Lip_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id-lip");

            //given: case where respondent1 Lip has email and callback for notify respondent1 is triggered
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to respondent1
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "template-id-lip",
                getRespondentNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent1Lip_whenInvokedBilingual() {
            when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh())
                .thenReturn("template-id-lip-translate");

            //given: case where respondent1 Lip has email and callback for notify respondent1 is triggered
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build())
                                 .build())
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to respondent1
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "template-id-lip-translate",
                getRespondentNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyCOORespondent1Lip_whenInvokedBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual())
                .thenReturn("template-id-lip-translate");

            //given: case where respondent1 Lip has email and callback for notify respondent1 is triggered
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build())
                                 .build())
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to respondent1
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "template-id-lip-translate",
                getRespondentNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Lip_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id-lip");

            //given: case where respondent2 Lip has email and callback for notify respondent2 is triggered
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .respondent2Represented(YesOrNo.NO)
                .respondent2(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualTitle("Mr.")
                                 .individualFirstName("Alex")
                                 .individualLastName("Richards")
                                 .partyName("Mr. Alex Richards")
                                 .partyEmail("respondentLip2@gmail.com")
                                 .build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to respondent2
            verify(notificationService).sendMail(
                "respondentLip2@gmail.com",
                "template-id-lip",
                getRespondent2NotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2Lip_whenInvokedBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id-lip");

            //given: case where respondent2 Lip has email and callback for notify respondent2 is triggered
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .respondent1Represented(YesOrNo.NO)
                .respondent2Represented(YesOrNo.NO)
                .respondent2(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .individualTitle("Mr.")
                                 .individualFirstName("Alex")
                                 .individualLastName("Richards")
                                 .partyName("Mr. Alex Richards")
                                 .partyEmail("respondentLip2@gmail.com")
                                 .build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to respondent2
            verify(notificationService).sendMail(
                "respondentLip2@gmail.com",
                "template-id-lip",
                getRespondent2NotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id-lip");

            //given: case where applicant Lip has email and notify for applicant is called
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to applicant
            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id-lip",
                getApplicantNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedBilingual() {
            when(notificationsProperties.getOrderBeingTranslatedTemplateWelsh())
                .thenReturn("template-id-lip-translate");

            //given: case where applicant Lip has email & bilingual flag is on and notify for applicant is called
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to applicant
            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id-lip-translate",
                getApplicantNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyCOOApplicantLip_whenInvokedBilingual() {
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual())
                .thenReturn("template-id-lip-translate");

            //given: case where applicant Lip has email & bilingual flag is on and notify for applicant is called
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference("BOTH").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER.name()).build()
            ).build();
            //when: handler is called
            handler.handle(params);
            //then: email should be sent to applicant
            verify(notificationService).sendMail(
                "rambo@email.com",
                "template-id-lip-translate",
                getApplicantNotificationDataMapLip(caseData),
                "generate-order-notification-000DC001"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()).build()))
                .isEqualTo(TASK_ID_APPLICANT);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT1);

            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
                CallbackRequest.builder().eventId(
                    NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER.name()).build()).build()))
                .isEqualTo(TASK_ID_RESPONDENT2);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, handler.getLegalOrganizationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001",
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

        @NotNull
        private Map<String, String> getRespondentNotificationDataMapLip(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_NAME, caseData.getRespondent1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }

        @NotNull
        private Map<String, String> getRespondent2NotificationDataMapLip(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER,  caseData.getCcdCaseReference().toString(),
                PARTY_NAME, caseData.getRespondent2().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }

        @NotNull
        private Map<String, String> getApplicantNotificationDataMapLip(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_NAME, caseData.getApplicant1().getPartyName(),
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
            );
        }
    }
}
