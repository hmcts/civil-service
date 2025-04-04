package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimUnspecNotifierTest {

    private static final Long CASE_ID = 1594901956117591L;

    @InjectMocks
    private AcknowledgeClaimUnspecNotifier acknowledgeClaimUnspecNotifier;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
    }

    @Test
    void shouldNotifyApplicantAndRespondent1Solicitor_whenResp1Acknowledged() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        final Set<EmailDTO> partiesToNotify = acknowledgeClaimUnspecNotifier.getPartiesToNotify(caseData);

        final EmailDTO applicantSolicitorNotification = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMapWhenResp1Acknowledged())
            .reference("acknowledge-claim-applicant-notification-000DC001")
            .build();

        Map<String, String> properties = new java.util.HashMap<>(getNotificationDataMapWhenResp1Acknowledged());

        properties.put(RESPONDENT_NAME, "Mr. Sole Trader");
        properties.put(RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE));

        final EmailDTO respondentSolicitorNotification = EmailDTO.builder()
            .targetEmail("respondentsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(properties)
            .reference("acknowledge-claim-respondent-notification-000DC001")
            .build();

        final Set<EmailDTO> expectedNotifications = Set.of(applicantSolicitorNotification, respondentSolicitorNotification);

        assertThat(partiesToNotify).containsAll(expectedNotifications);
        assertThat(partiesToNotify.size()).isEqualTo(expectedNotifications.size());
    }

    @Test
    void shouldNotifyApplicantAndRespondent2Solicitor_whenResp2Acknowledged() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
            .respondent1AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
            .respondent2ResponseDeadline(LocalDateTime.now().plusDays(12))
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
            .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1ClaimResponseIntentionType(ResponseIntention.CONTEST_JURISDICTION)
            .build();

        final Set<EmailDTO> partiesToNotify = acknowledgeClaimUnspecNotifier.getPartiesToNotify(caseData);

        final EmailDTO applicantSolicitorNotification = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMapWhenResp2Acknowledged())
            .reference("acknowledge-claim-applicant-notification-000DC001")
            .build();

        Map<String, String> properties = new java.util.HashMap<>(getNotificationDataMapWhenResp2Acknowledged());

        properties.put(RESPONDENT_NAME, "Mr. John Rambo");
        properties.put(RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent2ResponseDeadline().toLocalDate(), DATE));

        final EmailDTO respondentSolicitor2Notification = EmailDTO.builder()
            .targetEmail("respondentsolicitor2@example.com")
            .emailTemplate("template-id")
            .parameters(properties)
            .reference("acknowledge-claim-respondent-notification-000DC001")
            .build();

        final Set<EmailDTO> expectedNotifications = Set.of(
            applicantSolicitorNotification, respondentSolicitor2Notification
        );

        assertThat(partiesToNotify).containsAll(expectedNotifications);
        assertThat(partiesToNotify.size()).isEqualTo(expectedNotifications.size());
    }

    @NotNull
    private Map<String, String> getNotificationDataMapWhenResp1Acknowledged() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001",
            RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim"
        );
    }

    @NotNull
    private Map<String, String> getNotificationDataMapWhenResp2Acknowledged() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001",
            RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim"
        );
    }
}
