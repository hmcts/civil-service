package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
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
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
class AddDefendantLitigationFriendNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;

    @InjectMocks
    AddDefendantLitigationFriendNotifier addDefendantLitigationFriendNotifier;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @BeforeEach
    public void setUp() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(notificationsProperties.getSolicitorLitigationFriendAdded()).thenReturn("template-id");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("org name").build()));
    }

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        final Set<EmailDTO> partiesToNotify = addDefendantLitigationFriendNotifier.getPartiesToNotify(caseData);
        final EmailDTO party1 = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("litigation-friend-added-applicant-notification-000DC001")
            .build();

        final EmailDTO party2 = EmailDTO.builder()
            .targetEmail("respondentsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("litigation-friend-added-respondent-notification-000DC001")
            .build();

        final Set<EmailDTO> expected = Set.of(party1, party2);

        assertThat(partiesToNotify.containsAll(expected)).isEqualTo(true);
        assertThat(partiesToNotify.size()).isEqualTo(expected.size());
    }

    @Test
    void shouldNotifyRespondent2Solicitor_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        final Set<EmailDTO> partiesToNotify = addDefendantLitigationFriendNotifier.getPartiesToNotify(caseData);

        final EmailDTO party1 = EmailDTO.builder()
            .targetEmail("applicantsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("litigation-friend-added-applicant-notification-000DC001")
            .build();

        final EmailDTO party2 = EmailDTO.builder()
            .targetEmail("respondentsolicitor@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("litigation-friend-added-respondent-notification-000DC001")
            .build();

        final EmailDTO party3 = EmailDTO.builder()
            .targetEmail("respondentsolicitor2@example.com")
            .emailTemplate("template-id")
            .parameters(getNotificationDataMap())
            .reference("litigation-friend-added-respondent-notification-000DC001")
            .build();

        final Set<EmailDTO> expected = Set.of(party1, party2, party3);

        assertThat(partiesToNotify.containsAll(expected)).isEqualTo(true);
        assertThat(partiesToNotify.size()).isEqualTo(expected.size());
    }

    @NotNull
    private Map<String, String> getNotificationDataMap() {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789",
            CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
            CASEMAN_REF, "000DC001"
        );
    }
}
