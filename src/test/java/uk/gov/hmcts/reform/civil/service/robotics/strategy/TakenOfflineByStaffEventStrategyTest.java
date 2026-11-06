package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
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

class TakenOfflineByStaffEventStrategyTest {

    @Mock
    private RoboticsManualOfflineSupport manualOfflineSupport;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private TakenOfflineByStaffEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(manualOfflineSupport.prepareTakenOfflineEventDetails(any())).thenReturn("RPA Reason: offline.");
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenDateMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenTakenOfflineByStaffDatePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateHistoryMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributePreservesExistingEventsAndOfflineDateWhenUnspecDetailsMissing() {
        LocalDateTime offlineDate = LocalDateTime.of(2024, 7, 1, 9, 30);
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(offlineDate)
            .build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setClaimProceedsInCaseman(null);
        when(sequenceGenerator.nextSequence(any())).thenReturn(4);
        Event existingEvent = new Event();
        existingEvent.setEventSequence(3);
        existingEvent.setDateReceived(offlineDate.minusDays(1));
        EventHistory eventHistory = new EventHistory().setMiscellaneous(List.of(existingEvent));
        TakenOfflineByStaffEventStrategy strategyWithRealSupport = new TakenOfflineByStaffEventStrategy(
            new RoboticsManualOfflineSupport(new RoboticsEventTextFormatter()), sequenceGenerator, stateFlowEngine);

        strategyWithRealSupport.contribute(eventHistory, caseData, null);

        assertThat(eventHistory.getMiscellaneous()).hasSize(2);
        assertThat(eventHistory.getMiscellaneous().getFirst()).isSameAs(existingEvent);
        Event offlineEvent = eventHistory.getMiscellaneous().get(1);
        assertThat(offlineEvent.getEventSequence()).isEqualTo(4);
        assertThat(offlineEvent.getEventCode()).isEqualTo("999");
        assertThat(offlineEvent.getDateReceived()).isEqualTo(offlineDate);
        assertThat(offlineEvent.getEventDetailsText()).isEqualTo("RPA Reason: Case taken offline by staff.");
        assertThat(offlineEvent.getEventDetails().getMiscText()).isEqualTo(offlineEvent.getEventDetailsText());
    }

    @Test
    void contributeAddsMiscellaneousEvent() {
        LocalDateTime offlineDate = LocalDate.of(2024, 7, 1).atStartOfDay();
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(offlineDate)
            .build();
        when(sequenceGenerator.nextSequence(any())).thenReturn(4);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(4);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(offlineDate);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText()).isEqualTo("RPA Reason: offline.");
    }
}
