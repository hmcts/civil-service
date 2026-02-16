package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SetAsideJudgmentStrategyTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private SetAsideJudgmentStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new SetAsideJudgmentStrategy(featureToggleService, sequenceGenerator);
    }

    @Test
    void supportsReturnsFalseWhenToggleDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleRespondentEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2024, 5, 1));
        caseData.setJoSetAsideApplicationDate(LocalDate.of(2024, 4, 10));
        caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2024, 5, 2, 9, 0));

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getSetAsideJudgment()).hasSize(1);
        assertThat(builder.getSetAsideJudgment().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getSetAsideJudgment().getFirst().getEventCode())
            .isEqualTo(EventType.SET_ASIDE_JUDGMENT.getCode());
        assertThat(builder.getSetAsideJudgment().getFirst().getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(builder.getSetAsideJudgment().getFirst().getEventDetails().getApplicant())
            .isEqualTo("PARTY AGAINST");
        assertThat(builder.getSetAsideJudgment().getFirst().getEventDetails().getApplicationDate())
            .isEqualTo(LocalDate.of(2024, 4, 10));
        assertThat(builder.getSetAsideJudgment().getFirst().getEventDetails().getResultDate())
            .isEqualTo(LocalDate.of(2024, 5, 1));
    }

    @Test
    void contributeAddsEventsForBothRespondents() {
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Alex");
        respondent2.setIndividualLastName("Jones");

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party();
        caseData.setRespondent2(respondent2);
        caseData.setAddRespondent2(YesOrNo.YES);
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
        caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2024, 5, 2, 10, 0));

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getSetAsideJudgment()).hasSize(2);
        assertThat(builder.getSetAsideJudgment().getFirst().getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(builder.getSetAsideJudgment().get(1).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT2_ID);
        assertThat(builder.getSetAsideJudgment())
            .allSatisfy(event -> assertThat(event.getEventDetails().getApplicant()).isEqualTo("PROPER OFFICER"));
    }

    @Test
    void contributeUsesDefenceDateWhenOrderAfterDefence() {
        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2024, 6, 10));
        caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2024, 5, 20));
        caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2024, 6, 11, 12, 0));

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(33);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getSetAsideJudgment()).singleElement().satisfies(event -> {
            assertThat(event.getEventDetails().getApplicant()).isEqualTo("PARTY AGAINST");
            assertThat(event.getEventDetails().getApplicationDate()).isEqualTo(LocalDate.of(2024, 5, 20));
            assertThat(event.getEventDetails().getResultDate()).isEqualTo(LocalDate.of(2024, 6, 10));
        });
    }
}
