package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class DefendantResponseApplicantForCuiNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private DefendantResponseApplicantForCuiNotificationHandler handler;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Nested
    class AboutToSubmitCallback {

        private static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
        private static final String REFERENCE = "defendant-response-applicant-notification-000DC001";
        private static final String APPLICANT_EMAIL = "rambo@email.com";
        private static final String TEMPLATE_ID = "template-id";
        private static final String TEMPLATE_ID_MEDIATION = "template-id-mediation";
        private static final String TEMPLATE_ID_NO_MEDIATION = "template-id-no-mediation";
        private static final String TEMPLATE_ID_LIP_CLAIMANT = "template-id-lip-claimant";
        private static final String CLAIM_LEGAL_ORG_NAME = "Signer Name";

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
        void shouldNotifyApplicantSolicitorForPartAdmit_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullAdmitOrPartAdmitTemplate())
                .thenReturn(TEMPLATE_ID);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorForFullDefenceWithMediation_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullDefenceWithMediationTemplate())
                .thenReturn(TEMPLATE_ID_MEDIATION);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .responseClaimMediationSpecRequired(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID_MEDIATION,
                getNotificationFullDefenceDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorForFullDefenceNoMediation_whenInvoked() {
            when(notificationsProperties.getRespondentLipFullDefenceNoMediationTemplate())
                .thenReturn(TEMPLATE_ID_NO_MEDIATION);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .responseClaimMediationSpecRequired(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_SOLICITOR_EMAIL,
                TEMPLATE_ID_NO_MEDIATION,
                getNotificationFullDefenceDataMap(caseData),
                REFERENCE
            );
        }

        @Test
        void shouldNotifyLiPClaimant_whenInvoked() {
            when(notificationsProperties.getNotifyLiPClaimantDefendantResponded()).thenReturn(TEMPLATE_ID_LIP_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                APPLICANT_EMAIL,
                TEMPLATE_ID_LIP_CLAIMANT,
                getNotificationDataMapForLiPClaimant(caseData),
                REFERENCE
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME);
            expectedProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            expectedProperties.put(CASEMAN_REF, "000DC001");
            expectedProperties.put("defendantName", "Mr. Sole Trader");
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationFullDefenceDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            expectedProperties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME);
            expectedProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            expectedProperties.put(CASEMAN_REF, "000DC001");
            return expectedProperties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapForLiPClaimant(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
            expectedProperties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
            expectedProperties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            return expectedProperties;
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
