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
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ResponseDeadlineExtensionClaimantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ResponseDeadlineExtensionClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private PinInPostConfiguration pipInPostConfiguration;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private ResponseDeadlineExtensionClaimantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final String emailTemplate = "emailTemplate2";
        private final String claimantEmail = "applicantsolicitor@example.com";
        private final String claimantLipEmail = "rambo@email.com";
        private final String emailLipTemplate = "emailTemplateLip";
        private final String emailLipWelshTemplate = "emailTemplateWelshLip";
        private final String legacyReference = "000DC001";

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
        void shouldSendEmailToClaimantLR() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondentSolicitor1AgreedDeadlineExtension(LocalDate.now())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            given(notificationsProperties.getClaimantDeadlineExtension()).willReturn(emailTemplate);
            handler.handle(params);

            verify(notificationService).sendMail(
                claimantEmail,
                emailTemplate,
                getNotificationDataMap(caseData),
                "claimant-deadline-extension-notification-" + legacyReference
            );
        }

        @Test
        void shouldSendEmailToClaimantLip() {
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            given(notificationsProperties.getClaimantLipDeadlineExtension()).willReturn(emailLipTemplate);
            when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("url");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1ResponseDeadline(LocalDateTime.now())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                claimantLipEmail,
                emailLipTemplate,
                getNotificationDataMapForLip(caseData),
                "claimant-deadline-extension-notification-" + legacyReference
            );
        }

        @Test
        void shouldSendEmailToClaimantLipInWelsh() {
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            given(notificationsProperties.getClaimantLipDeadlineExtensionWelsh()).willReturn(emailLipWelshTemplate);
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("url");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1ResponseDeadline(LocalDateTime.now())
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                claimantLipEmail,
                emailLipWelshTemplate,
                getNotificationDataMapForLip(caseData),
                "claimant-deadline-extension-notification-" + legacyReference
            );
        }

        @Test
        void shouldSendEmailToClaimantLipInEngish_ifPreLiPvLRReleaseDate() {
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            given(notificationsProperties.getClaimantLipDeadlineExtension()).willReturn(emailLipTemplate);
            when(toggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);
            when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("url");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .respondent1ResponseDeadline(LocalDateTime.now())
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            handler.handle(params);

            verify(notificationService).sendMail(
                claimantLipEmail,
                emailLipTemplate,
                getNotificationDataMapForLip(caseData),
                "claimant-deadline-extension-notification-" + legacyReference
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.putAll(Map.of(
                RESPONDENT_NAME, "Mr. Sole Trader",
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                AGREED_EXTENSION_DATE, formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE),
                PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            ));
            return expectedProperties;
        }


        @NotNull
        private Map<String, String> getNotificationDataMapForLip(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.putAll(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIMANT_NAME, "Mr. John Rambo",
                DEFENDANT_NAME, "Mr. Sole Trader",
                FRONTEND_URL, "url",
                RESPONSE_DEADLINE, formatLocalDate(
                    caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
            ));
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

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                "NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION").build()).build())).isEqualTo(TASK_ID);
    }
}
