package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;

@ExtendWith(MockitoExtension.class)
class ClaimDismissedNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;
    @InjectMocks
    ClaimDismissedNotifier claimDismissedNotifier;
    @Mock
    private  NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Test
    void shouldReturnOneParty() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline().build();

        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        State state = State.from(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName());
        when(stateFlow.getState()).thenReturn(state);

        final Set<EmailDTO> partiesToNotify = claimDismissedNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnTwoParties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline().build();

        StateFlow stateFlow = mock(StateFlow.class);

        State state = State.from(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        final Set<EmailDTO> partiesToNotify = claimDismissedNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnThreeParties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimDetailsNotificationDeadline().build();

        StateFlow stateFlow = mock(StateFlow.class);

        State state = State.from(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName());
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        final Set<EmailDTO> partiesToNotify = claimDismissedNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(3);
    }
}
