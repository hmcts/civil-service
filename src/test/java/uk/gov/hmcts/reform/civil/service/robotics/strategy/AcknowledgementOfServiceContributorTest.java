package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AcknowledgementOfServiceContributorTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private AcknowledgementOfServiceContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.NOTIFICATION_ACKNOWLEDGED.fullName()))
        );
        when(sequenceGenerator.nextSequence(any())).thenReturn(10, 11, 12, 13);
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 15, 9, 0));
    }

    @Test
    void supportsReturnsFalseWhenNoAckDate() {
        CaseData caseData = CaseData.builder().build();
        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleDefendantEvent() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Smith")
                .build())
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getAcknowledgementOfServiceReceived()).hasSize(1);
        assertThat(history.getAcknowledgementOfServiceReceived().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getAcknowledgementOfServiceReceived().get(0).getEventDetailsText())
            .isEqualTo("responseIntention: Defend all of the claim");
    }

    @Test
    void contributeAddsSpecEvent() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 2, 9, 0))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getAcknowledgementOfServiceReceived()).hasSize(1);
        assertThat(history.getAcknowledgementOfServiceReceived().get(0).getEventDetailsText())
            .isEqualTo("Defendant LR Acknowledgement of Service ");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsSameSolicitor() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Smith")
                .build())
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Jamie")
                .individualLastName("Roe")
                .build())
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondentResponseIsSame(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 11, 0))
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getAcknowledgementOfServiceReceived()).hasSize(2);
        assertThat(history.getAcknowledgementOfServiceReceived().get(0).getEventDetailsText())
            .contains("[1 of 2 - 2024-03-15]");
        assertThat(history.getAcknowledgementOfServiceReceived().get(1).getEventDetailsText())
            .contains("[2 of 2 - 2024-03-15]");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsTwoSolicitors() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Firm A")
                .build())
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 9, 0))
            .respondent2(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Firm B")
                .build())
            .respondent2ClaimResponseIntentionType(ResponseIntention.PART_DEFENCE)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 2, 9, 0))
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getAcknowledgementOfServiceReceived()).hasSize(2);
        assertThat(history.getAcknowledgementOfServiceReceived().get(0).getEventDetailsText())
            .contains("Defendant: Firm A has acknowledged");
        assertThat(history.getAcknowledgementOfServiceReceived().get(1).getEventDetailsText())
            .contains("Defendant: Firm B has acknowledged");
    }
}
