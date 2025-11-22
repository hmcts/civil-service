package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

class GeneralApplicationStrikeOutStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private GeneralApplicationStrikeOutStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenNoStrikeOutApplications() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStrikeOutApplicationPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
            .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
            .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsGeneralFormAndDefenceEvents() {
        CaseData caseData = CaseDataBuilder.builder()
            .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
            .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();

        assertThat(history.getGeneralFormOfApplication()).hasSize(1);
        assertThat(history.getGeneralFormOfApplication().get(0).getEventCode())
            .isEqualTo(EventType.GENERAL_FORM_OF_APPLICATION.getCode());
        assertThat(history.getGeneralFormOfApplication().get(0).getEventDetailsText())
            .isEqualTo("APPLICATION TO Strike Out");

        assertThat(history.getDefenceStruckOut()).hasSize(1);
        assertThat(history.getDefenceStruckOut().get(0).getEventCode())
            .isEqualTo(EventType.DEFENCE_STRUCK_OUT.getCode());
    }

    @Test
    void contributeSkipsWhenNoMatchingJudgeDecision() {
        CaseData caseData = CaseDataBuilder.builder()
            .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getGeneralFormOfApplication()).isNullOrEmpty();
        assertThat(history.getDefenceStruckOut()).isNullOrEmpty();
    }

    @Test
    void contributeHandlesMultipleApplications() {
        CaseData initial = CaseDataBuilder.builder()
            .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
            .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
            .build();

        Element<GeneralApplication> first = initial.getGeneralApplications().get(0);
        Element<GeneralApplication> second = new Element<>(UUID.randomUUID(), first.getValue().toBuilder()
            .litigiousPartyID("004")
            .build());

        CaseData caseData = initial.toBuilder()
            .generalApplications(List.of(first, second))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getGeneralFormOfApplication()).hasSize(2);
        assertThat(history.getDefenceStruckOut()).hasSize(2);
    }
}
