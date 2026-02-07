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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        strategy = new SummaryJudgmentStrategy(sequenceGenerator, formatter, timelineHelper);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(42);
    }

    @Test
    void supportsReturnsFalseWhenDefendantDetailsMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsGrantedEventWhenSingleDefendant() {
        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Mr. Smith");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefendantDetails(defendantDetails);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(42);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(EventType.MISCELLANEOUS.getCode());
        String expectedMessage = formatter.summaryJudgmentGranted();
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(expectedMessage);
    }

    @Test
    void contributeAddsRequestedEventWhenRespondentTwoSelected() {
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Alex");
        respondent2.setIndividualLastName("Jones");
        DynamicListElement element = new DynamicListElement();
        element.setCode(UUID.randomUUID().toString());
        element.setLabel("Mr. Smith");
        DynamicList defendantDetails = new DynamicList();
        defendantDetails.setValue(element);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent2(respondent2)
            .build();
        caseData.setDefendantDetails(defendantDetails);

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
