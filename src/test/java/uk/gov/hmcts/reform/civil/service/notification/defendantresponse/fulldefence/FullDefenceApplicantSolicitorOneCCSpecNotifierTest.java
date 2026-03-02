package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.assertj.core.api.AssertionsForClassTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

class FullDefenceApplicantSolicitorOneCCSpecNotifierTest {

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
    private FullDefenceApplicantSolicitorOneCCSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    void sendNotificationToSolicitorSpec_shouldNotifyRespondentSolicitorSpecDef1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(new Respondent1DQ())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "spec-respondent-template-id",
            getNotificationDataMapSpec(),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldGetRecipientEmail() {
        // Given
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1EmailAddress("solicitor1@example.com")
            .build();

        // When
        String recipient = notifier.getRecipient(caseData);

        // Then
        assertEquals("solicitor1@example.com", recipient);
    }

    @Test
    void shouldSendNotificationToSolicitor() {
        // Given
        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("template-id");

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("12345")
            .ccdCaseReference(CASE_ID)
            .respondent1ResponseDate(null)
            .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth()
                .setName("statementOfTruthName"))
            .respondent2(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .organisationName("org-name")
                             .build())
            .respondent2OrganisationPolicy(
                new OrganisationPolicy().setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("org-id")))
            .build();
        // When
        notifier.sendNotificationToSolicitor(caseData, "solicitor1@example.com");

        // Then
        verify(notificationService).sendMail(
            eq("solicitor1@example.com"),
            eq("template-id"),
            anyMap(),
            eq("defendant-response-applicant-notification-12345")
        );
    }

    @Test
    void shouldGetLegalOrganisationName() {
        // Given
        CaseData caseData = CaseData.builder()
            .respondent1DQ(null)
            .respondent2OrganisationPolicy(
                new OrganisationPolicy().setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("org-id")))
            .build();

        when(organisationService.findOrganisationById("org-id"))
            .thenReturn(Optional.of(new Organisation().setName("Org Name")));

        // When
        String organisationName = notifier.getLegalOrganisationName(caseData);

        // Then
        AssertionsForClassTypes.assertThat("Org Name").isEqualTo(organisationName);

    }

    @Test
    void sendNotificationToSolicitorSpecPart_shouldNotifyRespondentSolicitorSpecDef1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(new Respondent1DQ())
            .applicant1Represented(YesOrNo.YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction()).thenReturn("spec-respondent-template-id-action");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "spec-respondent-template-id-action",
            getNotificationDataMapSpec(),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMapSpec() {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
        properties.put("defendantName", "Mr. Sole Trader");
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
        properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        properties.put(CASEMAN_REF, "000DC001");
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
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
        expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        return expectedProperties;
    }

}
