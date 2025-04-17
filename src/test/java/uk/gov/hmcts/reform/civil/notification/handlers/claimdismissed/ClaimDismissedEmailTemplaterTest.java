package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;

class ClaimDismissedEmailTemplaterTest {

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimDismissedEmailTemplater emailTemplater;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTemplateIdForClaimDismissedWithin4Months() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getStateHistory())
            .thenReturn(List.of(State.from(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName())));
        when(notificationsProperties.getSolicitorClaimDismissedWithin4Months()).thenReturn("template-id-4-months");

        String templateId = emailTemplater.getTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id-4-months");
    }

    @Test
    void shouldReturnTemplateIdForClaimDismissedWithin14Days() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getStateHistory())
            .thenReturn(List.of(State.from(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())));
        when(notificationsProperties.getSolicitorClaimDismissedWithin14Days()).thenReturn("template-id-14-days");

        String templateId = emailTemplater.getTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id-14-days");
    }

    @Test
    void shouldReturnTemplateIdForClaimDismissedWithinDeadline() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getStateHistory())
            .thenReturn(List.of(State.from(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName())));
        when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn("template-id-deadline");

        String templateId = emailTemplater.getTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id-deadline");
    }

    @Test
    void shouldReturnDefaultTemplateIdWhenNoMatchingState() {
        CaseData caseData = mock(CaseData.class);
        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.getStateHistory())
            .thenReturn(List.of(State.from(CLAIM_ISSUED.fullName())));
        when(notificationsProperties.getSolicitorClaimDismissedWithinDeadline()).thenReturn("default-template-id");

        String templateId = emailTemplater.getTemplateId(caseData);

        assertThat(templateId).isEqualTo("default-template-id");
    }
}
