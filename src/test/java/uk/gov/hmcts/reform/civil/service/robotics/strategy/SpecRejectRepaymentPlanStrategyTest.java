package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SpecRejectRepaymentPlanStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 5, 10, 12, 0);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private SpecRejectRepaymentPlanStrategy strategy;
    private RoboticsEventTextFormatter formatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        strategy = new SpecRejectRepaymentPlanStrategy(
            sequenceGenerator,
            formatter,
            new RoboticsTimelineHelper(() -> NOW),
            stateFlowEngine
        );

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(25);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT.fullName())
        ));
    }

    @Test
    void supportsReturnsFalseWhenPlanAccepted() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .applicant1ResponseDate(NOW.plusDays(1))
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1ResponseDate(NOW.plusDays(1))
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenPlanRejectedAndStatePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1ResponseDate(NOW.plusDays(1))
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsManualDeterminationEvent() {
        CaseData base = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .atStateRespondent1v1FullAdmissionSpec()
            .applicant1ResponseDate(NOW.minusDays(1))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, base, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(25);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(formatter.manualDeterminationRequired());
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(NOW);
    }
}
