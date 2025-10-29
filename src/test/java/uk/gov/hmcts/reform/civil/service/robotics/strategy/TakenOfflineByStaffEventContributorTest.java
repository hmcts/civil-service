package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsManualOfflineSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TakenOfflineByStaffEventContributorTest {

    @Mock
    private RoboticsManualOfflineSupport manualOfflineSupport;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private TakenOfflineByStaffEventContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(manualOfflineSupport.prepareTakenOfflineEventDetails(any())).thenReturn("RPA Reason: offline.");
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenDateMissing() {
        CaseData caseData = CaseData.builder().build();
        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenTakenOfflineByStaffDatePresent() {
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateHistoryMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsMiscellaneousEvent() {
        LocalDateTime offlineDate = LocalDate.of(2024, 7, 1).atStartOfDay();
        CaseData caseData = CaseData.builder()
            .takenOfflineByStaffDate(offlineDate)
            .build();
        when(sequenceGenerator.nextSequence(any())).thenReturn(4);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(4);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(offlineDate);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo("RPA Reason: offline.");
    }
}
