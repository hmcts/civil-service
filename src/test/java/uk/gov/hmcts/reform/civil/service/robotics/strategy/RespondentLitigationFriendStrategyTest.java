package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenAtLeastOneDatePresent() {
        Party respondent1 = new Party();
        respondent1.setPartyName("Resp One");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .build();
        caseData.setRespondent1LitigationFriendCreatedDate(LocalDateTime.now());

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForEachRespondent() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Resp");
        respondent1.setIndividualLastName("One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Resp");
        respondent2.setIndividualLastName("Two");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .build();
        LocalDateTime r1Date = LocalDateTime.of(2024, 2, 7, 11, 0);
        LocalDateTime r2Date = LocalDateTime.of(2024, 2, 8, 12, 0);
        caseData.setRespondent1LitigationFriendCreatedDate(r1Date);
        caseData.setRespondent2LitigationFriendCreatedDate(r2Date);

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
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.COMPANY);
        respondent2.setCompanyName("Resp Two Ltd");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent2(respondent2)
            .build();
        caseData.setRespondent2LitigationFriendCreatedDate(r2Date);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(r2Date);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Litigation friend added for respondent: Resp Two Ltd");
    }
}
