package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
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

class UnregisteredDefendantStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    private UnregisteredDefendantStrategy strategy;
    private RoboticsEventTextFormatter textFormatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textFormatter = new RoboticsEventTextFormatter();
        strategy = new UnregisteredDefendantStrategy(
            sequenceGenerator,
            timelineHelper,
            textFormatter,
            stateFlowEngine
        );
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()))
        );
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 1, 8, 0));
        when(sequenceGenerator.nextSequence(any())).thenReturn(21);
    }

    @Test
    void supportsReturnsFalseWhenNoSubmittedDate() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
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
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(21);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 15, 0, 0));
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(textFormatter.unregisteredSolicitor("", "Def One"));
    }

    @Test
    void supportsReturnsFalseWhenOrganisationIsRegistered() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.now())
            .respondent1(Party.builder().companyName("Def One").type(Party.Type.COMPANY).build())
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrgRegistered(YesOrNo.YES)
            .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(
                uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("ORG1").build()
            ).build())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsEventsForMultipleUnregisteredDefendants() {
        when(sequenceGenerator.nextSequence(any())).thenReturn(30, 31);
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 2, 15, 0, 0))
            .respondent1(Party.builder().companyName("Def One").type(Party.Type.COMPANY).build())
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrgRegistered(YesOrNo.NO)
            .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
            .respondent2(Party.builder().companyName("Def Two").type(Party.Type.COMPANY).build())
            .respondent2Represented(YesOrNo.YES)
            .respondent2OrgRegistered(YesOrNo.NO)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(
                textFormatter.unregisteredSolicitor("[1 of 2 - 2024-03-01] ", "Def One"),
                textFormatter.unregisteredSolicitor("[2 of 2 - 2024-03-01] ", "Def Two")
            );
    }
}
