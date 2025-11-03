package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
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

class UnrepresentedAndUnregisteredDefendantStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private UnrepresentedAndUnregisteredDefendantStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()))
        );
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 2, 10, 0));
        when(sequenceGenerator.nextSequence(any())).thenReturn(31, 32);
        when(textFormatter.unrepresentedAndUnregisteredCombined("[1 of 2 - 2024-03-02] ",
            "Unrepresented defendant and unregistered defendant solicitor firm. Unrepresented defendant: Resp One"))
            .thenReturn("RPA Reason: [1 of 2 - 2024-03-02] Unrepresented defendant and unregistered defendant solicitor firm. Unrepresented defendant: Resp One");
        when(textFormatter.unrepresentedAndUnregisteredCombined("[2 of 2 - 2024-03-02] ",
            "Unrepresented defendant and unregistered defendant solicitor firm. Unregistered defendant solicitor firm: Firm One"))
            .thenReturn("RPA Reason: [2 of 2 - 2024-03-02] Unrepresented defendant and unregistered defendant solicitor firm. Unregistered defendant solicitor firm: Firm One");
    }

    @Test
    void supportsReturnsFalseWhenSubmittedDateMissing() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void contributeAddsBothEvents() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 2, 20, 0, 0))
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("One")
                .build())
            .respondent1Represented(YesOrNo.NO)
            .respondent2(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Firm One")
                .build())
            .respondent2Represented(YesOrNo.YES)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
            .respondent2OrgRegistered(YesOrNo.NO)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(31);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: [1 of 2 - 2024-03-02] Unrepresented defendant and unregistered defendant solicitor firm. Unrepresented defendant: Resp One");
        assertThat(history.getMiscellaneous().get(1).getEventSequence()).isEqualTo(32);
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("RPA Reason: [2 of 2 - 2024-03-02] Unrepresented defendant and unregistered defendant solicitor firm. Unregistered defendant solicitor firm: Firm One");
    }
}
