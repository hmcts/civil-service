package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimSubmissionLipClaimantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandlerTest.AboutToSubmitCallback.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;

@ExtendWith(MockitoExtension.class)
class ClaimSubmissionLipClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    private static final String CLAIMANT_EMAIL_ADDRESS = "individual.claimant@email.com";
    private static final String REFERENCE = "claim-submission-lip-claimant-notification-000DC001";
    private static final String TEMPLATE_ID = "template-id";
    private CaseData caseData = CaseData.builder()
        .applicant1(Party.builder()
                        .individualTitle("Mr.")
                        .individualFirstName("Claimant")
                        .individualLastName("Guy")
                        .type(Party.Type.INDIVIDUAL)
                        .partyEmail(CLAIMANT_EMAIL_ADDRESS)
                        .build())
        .respondent1(Party.builder()
                         .individualTitle("Mr.")
                         .individualFirstName("Defendant")
                         .individualLastName("Guy")
                         .type(Party.Type.INDIVIDUAL)
                         .build())
        .legacyCaseReference(LEGACY_CASE_REFERENCE)
        .build();

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private ClaimSubmissionLipClaimantNotificationHandler handler;

    @Test
    void shouldNotifyLipClaimantWhenInvoked() {
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
        // Given
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION.toString())
                .build()).build();
        when(notificationsProperties.getNotifyClaimantLipForClaimSubmissionTemplate()).thenReturn(
            TEMPLATE_ID);

        // When
        handler.handle(params);

        // Then
        verify(notificationService).sendMail(
            CLAIMANT_EMAIL_ADDRESS,
            TEMPLATE_ID,
            getNotificationDataMap(),
            REFERENCE
        );
    }

    @Test
    void shouldNotifyLipClaimantWhenEmailAddressIsNotPresent() {
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
        // Given
        caseData.getApplicant1().setPartyEmail(null);
        caseData = caseData.toBuilder()
            .claimantUserDetails(IdamUserDetails.builder()
                                     .build())
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION.toString())
                .build()).build();
        // When
        handler.handle(params);

        // Then
        verify(notificationService, times(0)).sendMail(
            CLAIMANT_EMAIL_ADDRESS,
            TEMPLATE_ID,
            getNotificationDataMap(),
            REFERENCE
        );
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            CaseEvent.NOTIFY_LIP_CLAIMANT_CLAIM_SUBMISSION.toString()).build()).build())).isEqualTo(TASK_ID);
    }

    @NotNull
    public Map<String, String> getNotificationDataMap() {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(RESPONDENT_NAME, "Mr. Defendant Guy");
        expectedProperties.put(CLAIMANT_NAME, "Mr. Claimant Guy");
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
