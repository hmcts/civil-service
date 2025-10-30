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
        CaseData caseData = CaseData.builder()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .build();

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleRespondentEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .joSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION)
            .joSetAsideOrderDate(LocalDate.of(2024, 5, 1))
            .joSetAsideApplicationDate(LocalDate.of(2024, 4, 10))
            .joSetAsideCreatedDate(LocalDateTime.of(2024, 5, 2, 9, 0))
            .build();

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getSetAsideJudgment()).hasSize(1);
        assertThat(history.getSetAsideJudgment().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getSetAsideJudgment().get(0).getEventCode())
            .isEqualTo(EventType.SET_ASIDE_JUDGMENT.getCode());
        assertThat(history.getSetAsideJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getSetAsideJudgment().get(0).getEventDetails().getApplicant())
            .isEqualTo("PARTY AGAINST");
        assertThat(history.getSetAsideJudgment().get(0).getEventDetails().getApplicationDate())
            .isEqualTo(LocalDate.of(2024, 4, 10));
        assertThat(history.getSetAsideJudgment().get(0).getEventDetails().getResultDate())
            .isEqualTo(LocalDate.of(2024, 5, 1));
    }

    @Test
    void contributeAddsEventsForBothRespondents() {
        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party()
            .toBuilder()
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Jones")
                .build())
            .addRespondent2(YesOrNo.YES)
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .joSetAsideCreatedDate(LocalDateTime.of(2024, 5, 2, 10, 0))
            .build();

        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getSetAsideJudgment()).hasSize(2);
        assertThat(history.getSetAsideJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getSetAsideJudgment().get(1).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT2_ID);
        assertThat(history.getSetAsideJudgment())
            .allSatisfy(event -> assertThat(event.getEventDetails().getApplicant()).isEqualTo("PROPER OFFICER"));
    }
}
