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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
public class TranslatedOrderNoticeUploadedClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private TranslatedOrderNoticeUploadedClaimantNotificationHandler handler;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    private static final String CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL = "template-id-bilingual-translation";
    private static final String CLAIMANT_LIP_EMAIL = "rambo@email.com";
    private static final String LEGACY_CASE_REF = "translated-order-notice-uploaded-claimant-notification-000DC001";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotNotifyApplicantLR_whenInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .applicant1Represented(YesOrNo.YES)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verifyNoInteractions(notificationService);

        }

        @Test
        void  shouldNotifyLipClaimantForOrderTranslatedDoc_whenBilingual() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
            // Given
            when(notificationsProperties.getNotifyLiPOrderTranslatedTemplate()).thenReturn(CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL);

            CaseData caseData = CaseDataBuilder.builder()
                    .atStatePendingClaimIssued()
                    .build().toBuilder()
                    .applicant1Represented(YesOrNo.NO)
                    .claimantBilingualLanguagePreference("BOTH")
                    .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
                            .build()).build();
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                CLAIMANT_LIP_EMAIL,
                CLAIMANT_LIP_EMAIL_TEMPLATE_BILINGUAL,
                getNotificationDataMapSpecClaimantLIP(caseData),
                LEGACY_CASE_REF
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpecClaimantLIP(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>();
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
            expectedProperties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
            expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
            expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
            expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
            expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
            expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
            expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
            expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
            return expectedProperties;
        }
    }

    @Test
    void  shouldNotNotifyLipClaimantForOrderTranslatedDoc_whenEnglish() {
        // Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE.name())
                .build()).build();
        // When
        handler.handle(params);

        // Then
        verifyNoInteractions(notificationService);
    }
}
