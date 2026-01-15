package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BreathingSpaceEventStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private BreathingSpaceEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new BreathingSpaceEventStrategy(sequenceGenerator);
    }

    @Test
    void supportsReturnsFalseWhenBreathingNull() {
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenBreathingPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setBreathing(new BreathingSpaceInfo());
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeNoopsWhenUnsupported() {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, CaseDataBuilder.builder().build(), null);
        assertThat(builder.build().getBreathingSpaceEntered()).isNullOrEmpty();
    }

    @Test
    void addsStandardBreathingSpaceEnterEvent() {
        LocalDate start = LocalDate.of(2024, 1, 15);
        when(sequenceGenerator.nextSequence(any())).thenReturn(1);

        BreathingSpaceInfo breathing = new BreathingSpaceInfo();
        breathing.setEnter(new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("REF-123")
            .setStart(start));
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setBreathing(breathing);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        LocalTime before = LocalTime.now();
        strategy.contribute(builder, caseData, null);
        LocalTime after = LocalTime.now();

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceEntered().get(0).getDateReceived().toLocalTime()).isAfterOrEqualTo(before);
        assertThat(history.getBreathingSpaceEntered().get(0).getDateReceived().toLocalTime()).isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        Event event = history.getBreathingSpaceEntered().get(0);
        assertThat(event.getEventSequence()).isEqualTo(1);
        assertThat(event.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_ENTERED.getCode());
        assertThat(event.getDateReceived().toLocalDate()).isEqualTo(start);
        assertThat(event.getEventDetailsText())
            .isEqualTo("Breathing space reference REF-123, actual start date " + start);
        assertThat(history.getBreathingSpaceLifted()).isNullOrEmpty();
    }

    @Test
    void addsEnterAndLiftWhenLiftInformationPresent() {
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);
        when(sequenceGenerator.nextSequence(any())).thenReturn(5, 6);

        BreathingSpaceInfo breathing = new BreathingSpaceInfo();
        breathing.setEnter(new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("REF-99")
            .setStart(start));
        breathing.setLift(new BreathingSpaceLiftInfo()
            .setExpectedEnd(end));
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setBreathing(breathing);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        LocalTime before = LocalTime.now();
        strategy.contribute(builder, caseData, null);
        LocalTime after = LocalTime.now();

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceEntered().get(0).getDateReceived().toLocalTime()).isAfterOrEqualTo(before);
        assertThat(history.getBreathingSpaceEntered().get(0).getDateReceived().toLocalTime()).isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceLifted().get(0).getDateReceived().toLocalTime()).isAfterOrEqualTo(before);
        assertThat(history.getBreathingSpaceLifted().get(0).getDateReceived().toLocalTime()).isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted()).hasSize(1);

        Event enterEvent = history.getBreathingSpaceEntered().get(0);
        Event liftEvent = history.getBreathingSpaceLifted().get(0);

        assertThat(enterEvent.getEventSequence()).isEqualTo(5);
        assertThat(enterEvent.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_ENTERED.getCode());
        assertThat(enterEvent.getDateReceived().toLocalDate()).isEqualTo(start);

        assertThat(liftEvent.getEventSequence()).isEqualTo(6);
        assertThat(liftEvent.getEventCode()).isEqualTo(EventType.BREATHING_SPACE_LIFTED.getCode());
        assertThat(liftEvent.getDateReceived().toLocalDate()).isEqualTo(end);
        assertThat(liftEvent.getEventDetailsText())
            .isEqualTo("Breathing space reference REF-99, actual end date " + end);
    }

    @Test
    void usesFallbackWhenStartDateMissing() {
        when(sequenceGenerator.nextSequence(any())).thenReturn(7);

        BreathingSpaceInfo breathing = new BreathingSpaceInfo();
        breathing.setEnter(new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference("REF-200"));
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setBreathing(breathing);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        Event event = builder.build().getBreathingSpaceMentalHealthEntered().get(0);
        assertThat(event.getDateReceived()).isAfterOrEqualTo(before);
        assertThat(event.getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(event.getEventSequence()).isEqualTo(7);
        assertThat(event.getEventCode()).isEqualTo(EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED.getCode());
        String prefix = "Breathing space reference REF-200, actual start date ";
        LocalDateTime detailsTimestamp = extractTimestamp(event.getEventDetailsText(), prefix);
        assertThat(detailsTimestamp).isAfterOrEqualTo(before);
        assertThat(detailsTimestamp).isBeforeOrEqualTo(after);
    }

    @Test
    void addsMentalHealthEnterAndLiftWhenLiftPresent() {
        LocalDate start = LocalDate.of(2024, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 30);
        when(sequenceGenerator.nextSequence(any())).thenReturn(11, 12);

        BreathingSpaceInfo breathing = new BreathingSpaceInfo();
        breathing.setEnter(new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference("MH-REF")
            .setStart(start));
        breathing.setLift(new BreathingSpaceLiftInfo()
            .setExpectedEnd(end));
        CaseData caseData = CaseDataBuilder.builder()
            .build();

        caseData.setBreathing(breathing);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        LocalTime before = LocalTime.now();
        strategy.contribute(builder, caseData, null);
        LocalTime after = LocalTime.now();

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceMentalHealthEntered().get(0).getDateReceived().toLocalTime())
            .isAfterOrEqualTo(before)
            .isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceMentalHealthLifted().get(0).getDateReceived().toLocalTime())
            .isAfterOrEqualTo(before)
            .isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceMentalHealthEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceMentalHealthLifted()).hasSize(1);
        assertThat(history.getBreathingSpaceMentalHealthLifted().get(0).getEventDetailsText())
            .isEqualTo("Breathing space reference MH-REF, actual end date " + end);
    }

    @Test
    void usesFallbackWhenEndDateMissing() {
        LocalDate start = LocalDate.of(2024, 7, 10);
        when(sequenceGenerator.nextSequence(any())).thenReturn(14, 15);

        BreathingSpaceInfo breathing = new BreathingSpaceInfo();
        breathing.setEnter(new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("REF-NO-END")
            .setStart(start));
        breathing.setLift(new BreathingSpaceLiftInfo());
        CaseData caseData = CaseDataBuilder.builder()
            .build();

        caseData.setBreathing(breathing);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(builder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        EventHistory history = builder.build();
        assertThat(history.getBreathingSpaceLifted().get(0).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getBreathingSpaceLifted().get(0).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getBreathingSpaceEntered()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted()).hasSize(1);
        assertThat(history.getBreathingSpaceLifted().get(0).getEventDetailsText())
            .isEqualTo("Breathing space reference REF-NO-END, ");
    }

    private LocalDateTime extractTimestamp(String details, String prefix) {
        assertThat(details).startsWith(prefix);
        return LocalDateTime.parse(details.substring(prefix.length()));
    }
}
