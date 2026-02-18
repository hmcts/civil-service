package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

class ConsentExtensionEventStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ConsentExtensionEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(5, 6, 7, 8);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName()))
        );
    }

    @Test
    void supportsReturnsFalseWhenNoExtensionsPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleDefendantExtensionEvent() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Defendant Ltd");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 10, 9, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 3, 25))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getConsentExtensionFilingDefence()).hasSize(1);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventSequence()).isEqualTo(5);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getLitigiousPartyID()).isEqualTo(RESPONDENT_ID);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventDetailsText())
            .isEqualTo("agreed extension date: 25 03 2024");
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventDetails().getAgreedExtensionDate())
            .isEqualTo("2024-03-25");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsDifferentSolicitors() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Defendant One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.COMPANY);
        respondent2.setCompanyName("Defendant Two");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 12, 10, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 4, 1))
            .respondent2(respondent2)
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent2TimeExtensionDate(LocalDateTime.of(2024, 3, 13, 11, 0))
            .respondentSolicitor2AgreedDeadlineExtension(LocalDate.of(2024, 4, 2))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getConsentExtensionFilingDefence()).hasSize(2);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventSequence()).isEqualTo(5);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventDetailsText())
            .isEqualTo("Defendant: Defendant One has agreed extension: 01 04 2024");
        assertThat(builder.getConsentExtensionFilingDefence().get(1).getEventSequence()).isEqualTo(6);
        assertThat(builder.getConsentExtensionFilingDefence().get(1).getEventDetailsText())
            .isEqualTo("Defendant: Defendant Two has agreed extension: 02 04 2024");
        assertThat(builder.getConsentExtensionFilingDefence().get(1).getLitigiousPartyID()).isEqualTo(RESPONDENT2_ID);
    }

    @Test
    void contributeAddsEventsForTwoDefendantsSameSolicitor() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Alex");
        respondent1.setIndividualLastName("Smith");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Jamie");
        respondent2.setIndividualLastName("Roe");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1TimeExtensionDate(LocalDateTime.of(2024, 3, 5, 9, 0))
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 3, 28))
            .respondent2(respondent2)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent2TimeExtensionDate(LocalDateTime.of(2024, 3, 6, 9, 0))
            .respondentSolicitor2AgreedDeadlineExtension(LocalDate.of(2024, 3, 29))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getConsentExtensionFilingDefence()).hasSize(2);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventSequence()).isEqualTo(5);
        assertThat(builder.getConsentExtensionFilingDefence().getFirst().getEventDetailsText())
            .isEqualTo("Defendant(s) have agreed extension: 28 03 2024");
        assertThat(builder.getConsentExtensionFilingDefence().get(1).getEventSequence()).isEqualTo(6);
        assertThat(builder.getConsentExtensionFilingDefence().get(1).getEventDetailsText())
            .isEqualTo("Defendant(s) have agreed extension: 29 03 2024");
    }

    @Test
    void contributeAddsEmptyListForSpecWhenAgreedDeadlinePresentWithoutTimeExtension() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM)
            .respondentSolicitor1AgreedDeadlineExtension(LocalDate.of(2024, 3, 30))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getConsentExtensionFilingDefence()).isEmpty();
    }
}
