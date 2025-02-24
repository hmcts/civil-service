package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.PART_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;
    @InjectMocks
    AcknowledgeClaimNotifier acknowledgeClaimNotifier;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    public void setUp() {
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenInvoked() {
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 10), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotNotifyApplicantSolicitor_whenRecipientIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(null).build())
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        // only respondent notification sent
        verify(notificationService, times(1)).sendMail(anyString(),
                anyString(), anyMap(), anyString());

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 10), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotNotifyRespondentSolicitor_whenRecipient1IsNull() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress(null)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        // only applicant notification sent
        verify(notificationService, times(1)).sendMail(anyString(),
                anyString(), anyMap(), anyString());

        verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 10), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotNotifyRespondentSolicitor_whenRecipient2IsNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(null)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent1ResponseDeadline(null)
                .respondent2ResponseDeadline(LocalDateTime.of(2025, 3, 21, 0, 0))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .respondentSolicitor2EmailAddress(null)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        // only applicant notification sent
        verify(notificationService, times(1)).sendMail(anyString(),
                anyString(), anyMap(), anyString());
    }

    @Test
    void shouldNotifyRespondentSolicitor_whenInvoked() {
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 10), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsFirst_whenInvoked() {
        //solicitor 2  acknowledges claim, solicitor 1 does not
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(null)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent1ResponseDeadline(null)
                .respondent2ResponseDeadline(LocalDateTime.of(2025, 3, 21, 0, 0))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. John Rambo",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 21), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsLast_whenInvoked() {
        //solicitor 2 acknowledges claim,solicitor 1 already acknowledged
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2025, 3, 5, 0, 0))
                .respondent1ResponseDeadline(LocalDateTime.of(2025, 3, 19, 0, 0))
                .respondent2ResponseDeadline(LocalDateTime.of(2025, 3, 21, 0, 0))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. John Rambo",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 21), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor1WhenSolicitor1RespondsLast_whenInvoked() {
        //solicitor 1 acknowledges claim,solicitor 2 already acknowledged
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 21, 0, 0))
                .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 19, 0, 0))
                .respondent2ResponseDeadline(LocalDateTime.of(2025, 3, 5, 0, 0))
                .respondent1ResponseDeadline(LocalDateTime.of(2025, 3, 7, 0, 0))
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 7), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor1v2SameSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 21, 0, 0))
                .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 19, 0, 0))
                .respondent2ResponseDeadline(LocalDateTime.of(2025, 3, 5, 0, 0))
                .respondent1ResponseDeadline(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 7), DATE),
                        RESPONSE_INTENTION, new StringBuilder().append("The acknowledgement response selected: \n")
                                .append("Defendant 1: Defend part of the claim\n")
                                .append("Defendant 2: Defend all of the claim").toString(),
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor2v1SameSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 21, 0, 0))
                .respondent1ResponseDeadline(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent1ClaimResponseIntentionTypeApplicant2(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 7), DATE),
                        RESPONSE_INTENTION, new StringBuilder().append("The acknowledgement response selected: \n")
                                .append("Against Claimant 1: Defend part of the claim\n")
                                .append("Against Claimant 2: Defend all of the claim").toString(),
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }

    @Test
    void shouldNotifyRespondentSolicitor1_whenInvoked() {
        //solicitor 1 acknowledges claim,solicitor 2 not
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2025, 2, 21, 0, 0))
                .respondent2AcknowledgeNotificationDate(null)
                .respondent1ResponseDeadline(LocalDateTime.of(2025, 3, 7, 0, 0))
                .respondent2ResponseDeadline(null)
                .build();

        acknowledgeClaimNotifier.notifyParties(caseData);

        verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                Map.of(
                        CLAIM_REFERENCE_NUMBER, "1594901956117591",
                        RESPONDENT_NAME, "Mr. Sole Trader",
                        PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
                        RESPONSE_DEADLINE, formatLocalDate(LocalDate.of(2025, 3, 7), DATE),
                        RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim",
                        CLAIM_LEGAL_ORG_NAME_SPEC, "org name"
                ),
                "acknowledge-claim-applicant-notification-000DC001"
        );
    }
}