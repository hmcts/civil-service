package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.YamlNotificationTestUtil;
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

import java.util.HashMap;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
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

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getGenerateOrderNotificationTemplate()).thenReturn("template-id");

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

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, handler.getLegalOrganizationName(caseData));
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, "000DC001");
            return properties;
        }

        @NotNull
        private Map<String, String> getRespondentNotificationDataMapLip(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
            return properties;
        }

        @NotNull
        private Map<String, String> getRespondent2NotificationDataMapLip(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_NAME, caseData.getRespondent2().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
            return properties;
        }

        @NotNull
        private Map<String, String> getApplicantNotificationDataMapLip(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
            properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
            return properties;
        }

        @NotNull
        public Map<String, String> addCommonProperties() {
            Map<String, String> expectedProperties = new HashMap<>();
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
