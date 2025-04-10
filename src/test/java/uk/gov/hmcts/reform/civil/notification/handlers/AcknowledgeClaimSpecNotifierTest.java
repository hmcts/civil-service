package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecNotifierTest {

    private static final Long CASE_ID = 1594901956117591L;
    private static final String LEGACY_REF = "000DC001";
    private static final String TEMPLATE_ID = "template-id";
    private static final String ORG_NAME = "org name";

    private final LocalDate fixedIssueDate = LocalDate.now();
    private final LocalDate fixedResponseDeadlineDate = fixedIssueDate.plusDays(14);

    @InjectMocks
    private AcknowledgeClaimSpecNotifier acknowledgeClaimSpecNotifier;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        when(notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec()).thenReturn(TEMPLATE_ID);
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec()).thenReturn(TEMPLATE_ID);
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        final Set<EmailDTO> partiesToNotify = acknowledgeClaimSpecNotifier.getPartiesToNotify(caseData);

        Map<String, String> expectedApplicantParams = new HashMap<>();
        expectedApplicantParams.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
        expectedApplicantParams.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        expectedApplicantParams.put(RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim");
        expectedApplicantParams.put(CASEMAN_REF, LEGACY_REF);
        expectedApplicantParams.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
        expectedApplicantParams.put(ISSUED_ON, formatLocalDate(fixedIssueDate, DATE));

        EmailDTO expectedApplicantNotification = EmailDTO.builder()
                .targetEmail("applicantsolicitor@example.com")
                .emailTemplate(TEMPLATE_ID)
                .parameters(expectedApplicantParams)
                .reference(String.format("acknowledge-claim-applicant-notification-%s", LEGACY_REF))
                .build();

        Map<String, String> expectedRespondentParams = new HashMap<>();
        expectedRespondentParams.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
        expectedRespondentParams.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        expectedRespondentParams.put(RESPONSE_INTENTION, "The acknowledgement response selected: Defend all of the claim");
        expectedRespondentParams.put(CASEMAN_REF, LEGACY_REF);
        expectedRespondentParams.put(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
        expectedRespondentParams.put(RESPONDENT_NAME, "Mr. Sole Trader");
        expectedRespondentParams.put(RESPONSE_DEADLINE, formatLocalDate(fixedResponseDeadlineDate, DATE));

        EmailDTO expectedRespondentNotification = EmailDTO.builder()
                .targetEmail("respondentsolicitor@example.com")
                .emailTemplate(TEMPLATE_ID)
                .parameters(expectedRespondentParams)
                .reference(String.format("acknowledge-claim-respondent-notification-%s", LEGACY_REF))
                .build();

        Set<EmailDTO> expectedNotifications = Set.of(expectedApplicantNotification, expectedRespondentNotification);

        assertThat(partiesToNotify).containsAll(expectedNotifications);
        assertThat(partiesToNotify.size()).isEqualTo(expectedNotifications.size());
    }
}
