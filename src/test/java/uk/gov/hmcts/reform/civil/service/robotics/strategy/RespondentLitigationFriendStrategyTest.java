package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RespondentLitigationFriendStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @InjectMocks
    private RespondentLitigationFriendStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any())).thenReturn(5, 6);
    }

    @Test
    void supportsReturnsFalseWhenNoLitigationFriendDates() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenAtLeastOneDatePresent() {
        CaseData caseData = CaseData.builder()
            .respondent1LitigationFriendCreatedDate(LocalDateTime.now())
            .respondent1(Party.builder().partyName("Resp One").build())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForEachRespondent() {
        LocalDateTime r1Date = LocalDateTime.of(2024, 2, 7, 11, 0);
        LocalDateTime r2Date = LocalDateTime.of(2024, 2, 8, 12, 0);
        CaseData caseData = CaseData.builder()
            .respondent1LitigationFriendCreatedDate(r1Date)
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("One")
                .build())
            .respondent2LitigationFriendCreatedDate(r2Date)
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("Two")
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(5);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(r1Date);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Litigation friend added for respondent: Resp One");
        assertThat(history.getMiscellaneous().get(1).getEventSequence()).isEqualTo(6);
        assertThat(history.getMiscellaneous().get(1).getDateReceived()).isEqualTo(r2Date);
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("Litigation friend added for respondent: Resp Two");
    }

    @Test
    void contributeAddsEventOnlyForSecondRespondent() {
        LocalDateTime r2Date = LocalDateTime.of(2024, 3, 10, 10, 30);
        CaseData caseData = CaseData.builder()
            .respondent2LitigationFriendCreatedDate(r2Date)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Resp Two Ltd").build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(r2Date);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Litigation friend added for respondent: Resp Two Ltd");
    }
}
