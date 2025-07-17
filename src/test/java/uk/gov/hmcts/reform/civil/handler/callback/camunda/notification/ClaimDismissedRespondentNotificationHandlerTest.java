package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed.ClaimDismissedEmailTemplater;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ClaimDismissedRespondentNotificationHandlerTest {

    public static final String TEMPLATE_ID_1 = "template-id-1";
    public static final String TEMPLATE_ID_2 = "template-id-2";
    public static final String TEMPLATE_ID_3 = "template-id-3";

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Mock
    private ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Spy
    private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    @InjectMocks
    private ClaimDismissedRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

            when(stateFlowEngine.getStateFlow(any(CaseData.class))).thenReturn(StateFlowDTO.builder()
                                                                                   .state(State.from("MAIN.DRAFT"))
                                                                                   .stateHistory(List.of(State.from("MAIN.DRAFT")))
                                                                                   .flags(Map.of()).build());
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

            when(claimDismissedEmailTemplater.getSolicitorClaimDismissedProperty(any(), any())).thenReturn(TEMPLATE_ID_1);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID_1,
                getNotificationDataMap(caseData),
                "claim-dismissed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenCaseDataAtStateClaimAcknowledgeAndCcdStateIsDismissed() {

            when(claimDismissedEmailTemplater.getSolicitorClaimDismissedProperty(any(), any())).thenReturn(TEMPLATE_ID_1);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDate(LocalDateTime.now())
                .claimDismissedDeadline(LocalDateTime.now().minusHours(4))
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID_1,
                getNotificationDataMap(caseData),
                "claim-dismissed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenPastClaimNotificationDeadline() {

            when(claimDismissedEmailTemplater.getSolicitorClaimDismissedProperty(any(), any())).thenReturn(TEMPLATE_ID_2);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimNotificationDeadline()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID_2,
                getNotificationDataMap(caseData),
                "claim-dismissed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenCaseDataIsPastClaimDetailsNotification() {

            when(claimDismissedEmailTemplater.getSolicitorClaimDismissedProperty(any(), any())).thenReturn(TEMPLATE_ID_3);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                    .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED")
                    .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID_3,
                getNotificationDataMap(caseData),
                "claim-dismissed-respondent-notification-000DC001"
            );
        }
    }

    @Test
    void shouldNotNotifyRespondentSolicitor1_whenRespondent1LiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
            .respondent1Represented(NO)
            .respondentSolicitor1EmailAddress(null)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED")
                         .build())
            .build();

        handler.handle(params);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyRespondentSolicitor2_whenRespondent2LiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
            .respondent2Represented(NO)
            .respondentSolicitor2EmailAddress(null)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED")
                         .build())
            .build();

        handler.handle(params);

        verifyNoInteractions(notificationService);
    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001"
        ));
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
