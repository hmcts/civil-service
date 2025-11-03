package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DefendantNoCDeadlineStrategyTest {

    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @InjectMocks
    private DefendantNoCDeadlineStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(textFormatter.claimMovedOfflineAfterNocDeadline())
            .thenReturn("RPA Reason: Claim moved offline after defendant NoC deadline has passed");
    }

    @Test
    void supportsReturnsFalseWhenTakenOfflineDateMissing() {
        CaseData caseData = CaseData.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenRespondent1MissesDeadline() {
        LocalDateTime offline = LocalDateTime.of(2024, 5, 1, 10, 0);
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(offline)
            .addLegalRepDeadlineRes1(offline.minusDays(1))
            .respondent1Represented(YesOrNo.NO)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventWhenDeadlineMissed() {
        LocalDateTime offline = LocalDateTime.of(2024, 6, 1, 9, 0);
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(offline)
            .addLegalRepDeadlineRes2(offline.minusDays(2))
            .respondent2Represented(YesOrNo.NO)
            .build();

        when(sequenceGenerator.nextSequence(any())).thenReturn(7);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(7);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(offline);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Claim moved offline after defendant NoC deadline has passed");
    }

    @Test
    void contributeNoopsWhenDeadlinesNotBreached() {
        LocalDateTime offline = LocalDateTime.of(2024, 6, 1, 9, 0);
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(offline)
            .addLegalRepDeadlineRes1(offline.plusDays(2))
            .respondent1Represented(YesOrNo.NO)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.build().getMiscellaneous()).isNullOrEmpty();
    }
}
