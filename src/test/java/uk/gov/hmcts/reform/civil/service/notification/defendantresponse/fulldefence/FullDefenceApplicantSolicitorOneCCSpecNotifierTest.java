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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;

class FullDefenceApplicantSolicitorOneCCSpecNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

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

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("template-id");

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

    private Map<String, String> getNotificationDataMapSpec() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            "defendantName", "Mr. Sole Trader",
            CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789"
        );
    }

}
