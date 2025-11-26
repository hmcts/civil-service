package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CaseProceedsInCasemanStrategyTest {

    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private CaseProceedsInCasemanStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(textFormatter.caseProceedsInCaseman()).thenReturn("RPA Reason: Case Proceeds in Caseman.");
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(Collections.emptyList());
    }

    @Test
    void supportsReturnsFalseWhenRequirementsMissing() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenTakenOfflineWithOrder() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .orderSDODocumentDJ(Document.builder().documentFileName("sdo.pdf").build())
            .build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenOrderPresentButNoTakenOfflineDate() {
        CaseData caseData = CaseData.builder()
            .orderSDODocumentDJ(Document.builder().documentFileName("sdo.pdf").build())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenDirectionsOrderDisabled() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenTakenOfflineByStaffAfterSdoConditionsMet() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .takenOfflineByStaffDate(LocalDateTime.now())
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenSDONotDrawnReasonPresent() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .reasonNotSuitableSDO(ReasonNotSuitableSDO.builder()
                .input("unforeseen complexities")
                .build())
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueForStandardSdoCaseDataBuilderScenario() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
            .build();
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO.fullName())));
        assertThat(strategy.supports(caseData)).isTrue();

        when(sequenceGenerator.nextSequence(any())).thenReturn(5);
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);
        assertThat(builder.build().getMiscellaneous()).hasSize(1);
    }

    @Test
    void supportsReturnsTrueWhenStateHistoryIndicatesTakenOfflineAfterSdo() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO.fullName())));
        CaseData caseData = CaseData.builder().build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenStateHistoryIndicatesTakenOfflineAfterSdoEvenIfStaffAlsoRecorded() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO.fullName())));
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEvent() {
        LocalDateTime offline = LocalDateTime.of(2024, 7, 1, 12, 0);
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(offline)
            .orderSDODocumentDJ(Document.builder().documentFileName("sdo.pdf").build())
            .build();

        when(sequenceGenerator.nextSequence(any())).thenReturn(3);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(3);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(offline);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Case Proceeds in Caseman.");
    }
}
