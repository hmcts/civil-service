package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class InterlocutoryJudgmentStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private RoboticsTimelineHelper timelineHelper;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private final RoboticsPartyLookup partyLookup = new RoboticsPartyLookup();

    private InterlocutoryJudgmentStrategy strategy;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new InterlocutoryJudgmentStrategy(
            sequenceGenerator,
            timelineHelper,
            partyLookup,
            new RoboticsEventTextFormatter(),
            stateFlowEngine
        );

        now = LocalDateTime.of(2024, 6, 1, 12, 0);
        when(timelineHelper.now()).thenReturn(now);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of());
    }

    @Test
    void supportsReturnsFalseWhenHearingSupportMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenSummaryJudgmentRequestedForSingleRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Defendant 1").build())
                .build())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenOnlyDefendantDetailsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Both").build())
                .build())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsSingleEventWhenOnlyOneRespondent() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        CaseData caseData = CaseData.builder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment()).hasSize(1);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventCode())
            .isEqualTo(EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode());
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isEqualTo(now);
        assertThat(history.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeAddsEventsForBothRespondentsWhenApplicable() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Jones")
                .build())
            .addRespondent2(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Both defendants").build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment()).hasSize(2);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(21);
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getInterlocutoryJudgment().get(1).getEventSequence()).isEqualTo(22);
        assertThat(history.getInterlocutoryJudgment().get(1).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT2_ID);
        assertThat(history.getInterlocutoryJudgment().get(1).getDateReceived()).isEqualTo(now);
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(new RoboticsEventTextFormatter().summaryJudgmentGranted());
    }

    @Test
    void contributeSkipsWhenSupportConditionFails() {
        CaseData caseData = CaseDataBuilder.builder().build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.build().getInterlocutoryJudgment()).isNullOrEmpty();
        verifyNoMoreInteractions(sequenceGenerator);
    }
}
