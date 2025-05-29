package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
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
    }

    @Test
    void sendNotificationToSolicitorSpec_shouldNotifyRespondentSolicitorSpecDef1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build();
        caseData = caseData.toBuilder().caseAccessCategory(SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("spec-respondent-template-id");
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("12345")
            .ccdCaseReference(CASE_ID)
            .respondent1ResponseDate(null)
            .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder()
                                                          .name("statementOfTruthName").build())
            .respondent2(Party.builder()
                             .type(Party.Type.ORGANISATION)
                             .organisationName("org-name")
                             .build())
            .respondent2OrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("org-id").build())
                    .build())
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
                OrganisationPolicy.builder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("org-id").build())
                    .build())
            .build();

        when(organisationService.findOrganisationById("org-id"))
            .thenReturn(Optional.of(Organisation.builder().name("Org Name").build()));

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
            .respondent1DQ(Respondent1DQ.builder().build())
            .applicant1Represented(YesOrNo.YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction()).thenReturn("spec-respondent-template-id-action");
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor@example.com",
            "spec-respondent-template-id-action",
            getNotificationDataMapSpec(),
            "defendant-response-applicant-notification-000DC001"
        );
    }

    private Map<String, String> getNotificationDataMapSpec() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CASEMAN_REF, "000DC001",
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

}
