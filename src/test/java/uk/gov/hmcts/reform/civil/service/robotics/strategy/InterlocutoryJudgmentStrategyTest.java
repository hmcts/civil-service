package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class InterlocutoryJudgmentStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private final RoboticsPartyLookup partyLookup = new RoboticsPartyLookup();

    private InterlocutoryJudgmentStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new InterlocutoryJudgmentStrategy(
            sequenceGenerator,
            partyLookup,
            new RoboticsEventTextFormatter()
        );
    }

    @Test
    void supportsReturnsFalseWhenHearingSupportMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenSummaryJudgmentRequestedForSingleRespondent() {
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("John");
        respondent2.setIndividualLastName("Rambo");
        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Defendant 1");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build();
        HearingSupportRequirementsDJ supportRequirements = new HearingSupportRequirementsDJ();
        caseData.setHearingSupportRequirementsDJ(supportRequirements);
        caseData.setRespondent2(respondent2);
        caseData.setAddRespondent2(YesOrNo.YES);
        caseData.setDefendantDetails(defendantDetails);

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenOnlyDefendantDetailsProvided() {
        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Both");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build();
        caseData.setDefendantDetails(defendantDetails);

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEventWhenOnlyDefendantDetailsProvided() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(15);

        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Both defendants");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setDefendantDetails(defendantDetails);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(new RoboticsEventTextFormatter().summaryJudgmentGranted());
    }

    @Test
    void contributeAddsSingleEventWhenOnlyOneRespondent() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        CaseData caseData = CaseDataBuilder.builder()
            .build();
        HearingSupportRequirementsDJ supportRequirements = new HearingSupportRequirementsDJ();
        caseData.setHearingSupportRequirementsDJ(supportRequirements);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getInterlocutoryJudgment()).hasSize(1);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventCode())
            .isEqualTo(EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode());
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeAddsEventsForBothRespondentsWhenApplicable() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Alex");
        respondent2.setIndividualLastName("Jones");
        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Both defendants");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build();
        HearingSupportRequirementsDJ supportRequirements = new HearingSupportRequirementsDJ();
        caseData.setHearingSupportRequirementsDJ(supportRequirements);
        caseData.setRespondent2(respondent2);
        caseData.setAddRespondent2(YesOrNo.YES);
        caseData.setDefendantDetails(defendantDetails);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getInterlocutoryJudgment().get(1).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getInterlocutoryJudgment().get(1).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getInterlocutoryJudgment()).hasSize(2);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(21);
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getInterlocutoryJudgment().get(1).getEventSequence()).isEqualTo(22);
        assertThat(history.getInterlocutoryJudgment().get(1).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT2_ID);
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(new RoboticsEventTextFormatter().summaryJudgmentGranted());
    }

    @Test
    void contributeSkipsWhenSupportConditionFails() {
        CaseData caseData = CaseDataBuilder.builder().build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.build().getInterlocutoryJudgment()).isNullOrEmpty();
        verifyNoMoreInteractions(sequenceGenerator);
    }
}
