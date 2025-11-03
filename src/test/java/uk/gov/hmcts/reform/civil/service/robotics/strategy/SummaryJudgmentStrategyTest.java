package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SummaryJudgmentStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private RoboticsTimelineHelper timelineHelper;

    private RoboticsEventTextFormatter formatter;

    private SummaryJudgmentStrategy strategy;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        strategy = new SummaryJudgmentStrategy(sequenceGenerator, formatter, timelineHelper);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(42);
        now = LocalDateTime.of(2024, 5, 1, 9, 30);
        when(timelineHelper.now()).thenReturn(now);
    }

    @Test
    void supportsReturnsFalseWhenDefendantDetailsMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsGrantedEventWhenSingleDefendant() {
        CaseData caseData = CaseData.builder()
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Mr. Smith").build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(42);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(now);
        String expectedMessage = formatter.summaryJudgmentGranted();
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(expectedMessage);
    }

    @Test
    void contributeAddsRequestedEventWhenRespondentTwoSelected() {
        CaseData caseData = CaseData.builder()
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Jones")
                .build())
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Mr. Smith").build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(42);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(EventType.MISCELLANEOUS.getCode());
        String expected = formatter.summaryJudgmentRequested();
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(expected);
    }
}
