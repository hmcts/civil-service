package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AcknowledgementOfServiceStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private AcknowledgementOfServiceStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.NOTIFICATION_ACKNOWLEDGED.fullName()))
        );
        when(sequenceGenerator.nextSequence(any())).thenReturn(10, 11, 12, 13);
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 15, 9, 0));
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfAckMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenCaseDataNull() {
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void contributeDoesNothingWhenNotSupported() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName()))
        );
        CaseData caseData = CaseDataBuilder.builder().build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).isNullOrEmpty();
        verifyNoInteractions(sequenceGenerator);
    }

    @Test
    void contributeAddsSingleDefendantEvent() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Alex");
        respondent1.setIndividualLastName("Smith");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(1);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .isEqualTo("responseIntention: Defend all of the claim");
    }

    @Test
    void contributeAddsSpecEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 2, 9, 0))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(1);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .isEqualTo("Defendant LR Acknowledgement of Service ");
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
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .respondent2(respondent2)
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondentResponseIsSame(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 11, 0))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(2);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .contains("[1 of 2 - 2024-03-15]");
        assertThat(builder.getAcknowledgementOfServiceReceived().get(1).getEventDetailsText())
            .contains("[2 of 2 - 2024-03-15]");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsTwoSolicitors() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Firm A");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.COMPANY);
        respondent2.setCompanyName("Firm B");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 9, 0))
            .respondent2(respondent2)
            .respondent2ClaimResponseIntentionType(ResponseIntention.PART_DEFENCE)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 2, 9, 0))
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(2);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .contains("Defendant: Firm A has acknowledged");
        assertThat(builder.getAcknowledgementOfServiceReceived().get(1).getEventDetailsText())
            .contains("Defendant: Firm B has acknowledged");
    }

    @Test
    void contributeDoesNothingWhenAcknowledgeDateMissingForSingleDefendant() {
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(null)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).isNullOrEmpty();
    }

    @Test
    void contributeAddsOnlyExistingAckForTwoSolicitors() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);
        respondent1.setCompanyName("Firm A");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.COMPANY);
        respondent2.setCompanyName("Firm B");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 9, 0))
            .respondent2(respondent2)
            .respondent2ClaimResponseIntentionType(null)
            .respondent2AcknowledgeNotificationDate(null)
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(1);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .contains("Defendant: Firm A has acknowledged");
    }

    @Test
    void contributeAddsEventsForTwoDefendantsSameSolicitorAndRespondedOnBehalfOfSecondResThenRecordSameIntentionForResp1() {
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
            .respondent1AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
            .respondent2(respondent2)
            .respondent2SameLegalRepresentative(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondentResponseIsSame(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES)
            .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .respondent2AcknowledgeNotificationDate(LocalDateTime.of(2024, 3, 1, 11, 0))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getAcknowledgementOfServiceReceived()).hasSize(2);
        assertThat(builder.getAcknowledgementOfServiceReceived().getFirst().getEventDetailsText())
            .contains("[1 of 2 - 2024-03-15]");
        assertThat(builder.getAcknowledgementOfServiceReceived().get(1).getEventDetailsText())
            .contains("[2 of 2 - 2024-03-15]");
        assertThat(builder.getAcknowledgementOfServiceReceived().get(1).getEventDetails().getResponseIntention())
            .isEqualTo(ResponseIntention.FULL_DEFENCE.getLabel());
    }
}
