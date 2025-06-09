package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
public class NotifyClaimantClaimSubmittedTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private PinInPostConfiguration pinInPostConfiguration;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private NotifyClaimantClaimSubmitted handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String EMAIL_TEMPLATE_HWF = "test-notification-id";
        private static final String EMAIL_TEMPLATE_NO_HWF = "test-notification-no-hwf-id";
        private static final String EMAIL_TEMPLATE_NO_HWF_BILINGUAL = "test-notification-no-hwf-bilingual-id";
        private static final String EMAIL_TEMPLATE_HWF_BILINGUAL = "test-notification-bilingual-id";
        private static final String CLAIMANT_EMAIL_ID = "testorg@email.com";
        private static final String CLAIMANT_EMAIL_ID_INDIVIDUAL = "rambo@email.com";
        private static final String REFERENCE_NUMBER = "claim-submitted-notification-000DC001";
        private static final String CLAIMANT = "Mr. John Rambo";
        private static final String RESPONDENT_NAME = "Mr. Sole Trader";
        public static final String FRONTEND_CUI_URL = "dummy_cui_front_end_url";

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
        void shouldNotifyApplicant1_ClaimIsSubmittedButNotIssued() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(CLAIMANT_EMAIL_ID)
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_NO_HWF);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotSendEmail_whenEventIsCalledAndApplicantHasNoEmail() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_NO_HWF);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(0)).sendMail(
                CLAIMANT_EMAIL_ID,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHFWReferenceNumberPresent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("1111").build()).build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_HWF);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHFWReferanceNumberNotPresent() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate()).thenReturn(
                EMAIL_TEMPLATE_NO_HWF);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_NO_HWF,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHWFReferanceNumberNotPresentAndBilingual() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate()).thenReturn(
                EMAIL_TEMPLATE_NO_HWF_BILINGUAL);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_NO_HWF_BILINGUAL,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldSendEmail_whenHWFReferenceNumberPresentAndBilingual() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .caseDataLiP(CaseDataLiP.builder()
                                 .helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("1111").build())
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .build();

            // When
            when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate()).thenReturn(
                EMAIL_TEMPLATE_HWF_BILINGUAL);
            when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");
            when(toggleService.isLipVLipEnabled()).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID_INDIVIDUAL,
                EMAIL_TEMPLATE_HWF_BILINGUAL,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMap() {
            Map<String, String> expectedProperties = new HashMap<>(Map.of(
                CLAIMANT_NAME, CLAIMANT,
                DEFENDANT_NAME, RESPONDENT_NAME,
                FRONTEND_URL, FRONTEND_CUI_URL
            ));
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            return expectedProperties;
        }

    }
}
