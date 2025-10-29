package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RespondentCounterClaimContributorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 5, 15, 10, 30);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private RespondentCounterClaimContributor contributor;
    private RoboticsEventTextFormatter formatter;
    private RoboticsRespondentResponseSupport respondentResponseSupport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        respondentResponseSupport = new RoboticsRespondentResponseSupport(
            formatter,
            new RoboticsTimelineHelper(() -> NOW)
        );
        contributor = new RespondentCounterClaimContributor(
            sequenceGenerator,
            respondentResponseSupport,
            stateFlowEngine
        );

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.COUNTER_CLAIM.fullName())
        ));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoResponsesPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenCounterClaimPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();

        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventForSingleRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentCounterClaim()
            .respondent1ResponseDate(NOW.minusDays(1))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(formatter.defendantRejectsAndCounterClaims());
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(NOW.minusDays(1));
    }

    @Test
    void contributeAddsEventsForBothRespondents() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentCounterClaim()
            .respondent1ResponseDate(NOW.minusDays(2))
            .respondent2ResponseDate(NOW.minusDays(1))
            .respondent2ClaimResponseType(RespondentResponseType.COUNTER_CLAIM)
            .respondentResponseIsSame(YesOrNo.YES)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(respondentResponseSupport.prepareRespondentResponseText(
                caseData, caseData.getRespondent1(), true));
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo(respondentResponseSupport.prepareRespondentResponseText(
                caseData, caseData.getRespondent2(), false));
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 11);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getDateReceived)
            .containsExactly(NOW.minusDays(2), NOW.minusDays(1));
    }
}
