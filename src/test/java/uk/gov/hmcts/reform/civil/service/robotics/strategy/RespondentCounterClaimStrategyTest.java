package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RespondentCounterClaimStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentCounterClaimStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(
            new RoboticsEventTextFormatter(),
            timelineHelper
        );
        strategy = new RespondentCounterClaimStrategy(sequenceGenerator, respondentResponseSupport);
    }

    @Test
    void supportsReturnsFalseWhenNoResponsesPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueForUnspecCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentCounterClaim()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueForSpecCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateRespondentCounterClaimSpec()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscForSingleRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentCounterClaim()
            .respondent1(PartyBuilder.builder().individual("Alex").build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventCode)
            .containsExactly(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous())
            .extracting(Event::getDateReceived)
            .containsExactly(caseData.getRespondent1ResponseDate());
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(respondentResponseSupport.prepareRespondentResponseText(
                caseData,
                caseData.getRespondent1(),
                true
            ));
    }

    @Test
    void contributeAddsMiscForSameSolicitorSameResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.COUNTER_CLAIM)
            .respondent1(PartyBuilder.builder().individual("Alex").build())
            .respondent2(PartyBuilder.builder().individual("Ben").build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 11);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true),
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false)
            );
    }

    @Test
    void contributeAddsMiscWhenOnlySecondRespondentHasCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondent2CounterClaimAfterNotifyDetails()
            .respondent2(PartyBuilder.builder().individual("Ben").build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(respondentResponseSupport.prepareRespondentResponseText(
                caseData,
                caseData.getRespondent2(),
                false
            ));
    }

    @Test
    void contributeAddsMiscForSpecCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateRespondentCounterClaimSpec()
            .respondent1(PartyBuilder.builder().individual("Casey").build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(respondentResponseSupport.prepareRespondentResponseText(
                caseData,
                caseData.getRespondent1(),
                true
            ));
    }
}
