package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
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

class FullDefenceRespondentSolicitorOneCCSpecNotifierTest {

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
    private FullDefenceRespondentSolicitorOneCCSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
        when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
        when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
        when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
        when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
        when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
        when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
        when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
        when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecDef1_whenInvokedWithCcEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "spec-respondent-template-id",
            getNotificationDataMapPartAdmissionSpec(),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitorSpecDef1SecondScenerio_whenInvokedWithCcEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("my company").build())
            .build();
        CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC")
                    .build())
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            ArgumentMatchers.eq("respondentsolicitor2@example.com"),
            ArgumentMatchers.eq("spec-respondent-template-id"),
            ArgumentMatchers.argThat(map -> {
                Map<String, String> expected = getNotificationDataMapSpec();
                return map.get(CLAIM_REFERENCE_NUMBER).equals(expected.get(CLAIM_REFERENCE_NUMBER))
                    && map.get(CLAIM_LEGAL_ORG_NAME_SPEC).equals(expected.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            }),
            ArgumentMatchers.eq("defendant-response-applicant-notification-000DC001")
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMapPartAdmissionSpec() {
        Map<String, String> notificationData = new HashMap<>(addCommonProperties());
        notificationData.putAll(Map.of(
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        return notificationData;
    }

    @NotNull
    private Map<String, String> getNotificationDataMapSpec() {
        Map<String, String> notificationData = new HashMap<>(addCommonProperties());
        notificationData.putAll(Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001"
        ));
        return notificationData;
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
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }
}
