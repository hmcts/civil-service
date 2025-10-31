package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BreathingSpaceEventStrategyTest {

    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @InjectMocks
    private BreathingSpaceEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void supportsReturnsFalseWhenBreathingNull() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenBreathingPresent() {
        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder().build())
            .build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeNoopsWhenUnsupported() {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, CaseData.builder().build(), null);
        assertThat(builder.build().getBreathingSpaceEntered()).isNullOrEmpty();
    }

    @Test
    void addsStandardBreathingSpaceEnterEvent() {
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDateTime fallback = LocalDateTime.of(2024, 1, 15, 10, 30);
        when(timelineHelper.now()).thenReturn(fallback);
        when(sequenceGenerator.nextSequence(any())).thenReturn(1);

        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                .enter(BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.STANDARD)
                    .reference("REF-123")
                    .start(start)
                    .build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        Event event = history.getBreathingSpaceEntered().get(0);
        assertThat(event.getEventSequence()).isEqualTo(1);
        assertThat(event.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_ENTERED.getCode());
        assertThat(event.getDateReceived()).isEqualTo(start.atTime(fallback.toLocalTime()));
        assertThat(event.getEventDetailsText())
            .isEqualTo("Breathing space reference REF-123, actual start date " + start);
        assertThat(history.getBreathingSpaceLifted()).isNullOrEmpty();
    }

    @Test
    void addsEnterAndLiftWhenLiftInformationPresent() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);
        LocalDateTime fallbackEnter = LocalDateTime.of(2024, 2, 1, 9, 0);
        LocalDateTime fallbackLift = LocalDateTime.of(2024, 3, 1, 9, 0);
        when(timelineHelper.now()).thenReturn(fallbackEnter, fallbackLift);
        when(sequenceGenerator.nextSequence(any())).thenReturn(5, 6);

        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                .enter(BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.STANDARD)
                    .reference("REF-99")
                    .start(start)
                    .build())
                .lift(BreathingSpaceLiftInfo.builder()
                    .expectedEnd(end)
                    .build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted()).hasSize(1);

        Event enterEvent = history.getBreathingSpaceEntered().get(0);
        Event liftEvent = history.getBreathingSpaceLifted().get(0);

        assertThat(enterEvent.getEventSequence()).isEqualTo(5);
        assertThat(enterEvent.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_ENTERED.getCode());
        assertThat(enterEvent.getDateReceived()).isEqualTo(start.atTime(fallbackEnter.toLocalTime()));

        assertThat(liftEvent.getEventSequence()).isEqualTo(6);
        assertThat(liftEvent.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_LIFTED.getCode());
        assertThat(liftEvent.getDateReceived()).isEqualTo(end.atTime(fallbackLift.toLocalTime()));
        assertThat(liftEvent.getEventDetailsText())
            .isEqualTo("Breathing space reference REF-99, actual end date " + end);
    }

    @Test
    void usesFallbackWhenStartDateMissing() {
        LocalDateTime fallback = LocalDateTime.of(2024, 4, 5, 13, 15);
        when(timelineHelper.now()).thenReturn(fallback);
        when(sequenceGenerator.nextSequence(any())).thenReturn(7);

        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                .enter(BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.MENTAL_HEALTH)
                    .reference("REF-200")
                    .build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        Event event = builder.build().getBreathingSpaceMentalHealthEntered().get(0);
        assertThat(event.getEventSequence()).isEqualTo(7);
        assertThat(event.getEventCode()).isEqualTo(EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED.getCode());
        assertThat(event.getDateReceived()).isEqualTo(fallback);
        assertThat(event.getEventDetailsText())
            .isEqualTo("Breathing space reference REF-200, actual start date " + fallback);
    }

    @Test
    void addsMentalHealthEnterAndLiftWhenLiftPresent() {
        LocalDate start = LocalDate.of(2024, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 30);
        LocalDateTime fallbackEnter = LocalDateTime.of(2024, 6, 1, 8, 0);
        LocalDateTime fallbackLift = LocalDateTime.of(2024, 6, 30, 8, 0);
        when(timelineHelper.now()).thenReturn(fallbackEnter, fallbackLift);
        when(sequenceGenerator.nextSequence(any())).thenReturn(11, 12);

        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                .enter(BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.MENTAL_HEALTH)
                    .reference("MH-REF")
                    .start(start)
                    .build())
                .lift(BreathingSpaceLiftInfo.builder()
                    .expectedEnd(end)
                    .build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceMentalHealthEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceMentalHealthLifted()).hasSize(1);
        assertThat(history.getBreathingSpaceMentalHealthLifted().get(0).getEventDetailsText())
            .isEqualTo("Breathing space reference MH-REF, actual end date " + end);
    }

    @Test
    void usesFallbackWhenEndDateMissing() {
        LocalDate start = LocalDate.of(2024, 7, 10);
        LocalDateTime fallbackEnter = LocalDateTime.of(2024, 7, 10, 9, 0);
        LocalDateTime fallbackLift = LocalDateTime.of(2024, 7, 10, 9, 0);
        when(timelineHelper.now()).thenReturn(fallbackEnter, fallbackLift);
        when(sequenceGenerator.nextSequence(any())).thenReturn(14, 15);

        CaseData caseData = CaseData.builder()
            .breathing(BreathingSpaceInfo.builder()
                .enter(BreathingSpaceEnterInfo.builder()
                    .type(BreathingSpaceType.STANDARD)
                    .reference("REF-NO-END")
                    .start(start)
                    .build())
                .lift(BreathingSpaceLiftInfo.builder().build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted().get(0).getEventDetailsText())
            .isEqualTo("Breathing space reference REF-NO-END, ");
    }
}
