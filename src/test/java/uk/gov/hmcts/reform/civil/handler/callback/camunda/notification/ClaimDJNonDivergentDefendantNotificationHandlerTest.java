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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimSetAsideJudgmentClaimantNotificationHandlerTest.TEMPLATE_ID_LIP;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
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
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class ClaimDJNonDivergentDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private ClaimDJNonDivergentDefendantNotificationHandler handler;

    private static final String TEMPLATE_ID = "template-id";
    private static final String TASK_ID_RESPONDENT1 = "NotifyDJNonDivergentDefendant1";
    private static final String TASK_ID_RESPONDENT2 = "NotifyDJNonDivergentDefendant2";
    private static final String TASK_ID_RESPONDENT1_LIP = "NotifyDJNonDivergentDefendant1LiP";

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
        void shouldNotifyDefendantSolicitor1_whenInvoked() {
            when(notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "dj-non-divergent-defendant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor2_whenInvoked() {
            when(notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors().build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                TEMPLATE_ID,
                getNotificationDataMap(caseData),
                "dj-non-divergent-defendant-notification-000DC001"
            );

        }

        @Test
        void shouldNotifyDefendantLipSolicitor_whenInvoked() {
            when(notificationsProperties.getNotifyUpdateTemplate()).thenReturn(TEMPLATE_ID_LIP);

            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate().toBuilder()
                .applicant1(Party.builder()
                                .individualFirstName("Applicant1")
                                .individualLastName("ApplicantLastName").partyName("Applicant1")
                                .type(Party.Type.INDIVIDUAL).partyEmail("applicantLip@example.com").build())
                .respondent1(Party.builder().partyName("Respondent1")
                                 .individualFirstName("Respondent1").individualLastName("RespondentLastName")
                                 .type(Party.Type.INDIVIDUAL).partyEmail("respondentLip@example.com").build())
                .applicant1Represented(YesOrNo.NO)
                .legacyCaseReference("000DC001")
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .build();

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentLip@example.com",
                TEMPLATE_ID_LIP,
                getNotificationDataMapLip(caseData),
                "dj-non-divergent-defendant-notification-lip-000DC001"
            );
        }

    }

    @NotNull
    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        Map<String, String> map = new HashMap<>(addCommonProperties());
        map.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        map.put(LEGAL_ORG_NAME, "Test Org Name");
        map.put(DEFENDANT_NAME_INTERIM, "Mr. Sole Trader and Mr. John Rambo");
        map.put(CLAIMANT_NAME, "Mr. John Rambo");
        map.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        map.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return map;
    }

    @NotNull
    private Map<String, String> getNotificationDataMapLip(CaseData caseData) {
        Map<String, String> map = new HashMap<>(addCommonProperties());
        map.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        map.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        map.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        return map;
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

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(
            CallbackRequest.builder().eventId(
                NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name()).build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT1_LIP);
    }
}
