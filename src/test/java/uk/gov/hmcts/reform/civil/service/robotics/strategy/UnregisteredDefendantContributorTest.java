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

class UnregisteredDefendantContributorTest {

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
    private UnregisteredDefendantContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()))
        );
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 1, 8, 0));
        when(sequenceGenerator.nextSequence(any())).thenReturn(21);
        when(textFormatter.unregisteredSolicitor("", "Def One"))
            .thenReturn("RPA Reason: Unregistered defendant solicitor firm: Def One");
    }

    @Test
    void supportsReturnsFalseWhenNoSubmittedDate() {
        assertThat(contributor.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void contributeAddsEventForUnregisteredDefendant() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 2, 15, 0, 0))
            .respondent1(Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("Def One")
                .build())
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrgRegistered(YesOrNo.NO)
            .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(21);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 15, 0, 0));
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Unregistered defendant solicitor firm: Def One");
    }
}
