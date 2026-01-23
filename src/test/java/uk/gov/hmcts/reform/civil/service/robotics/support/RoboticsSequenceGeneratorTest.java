package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoboticsSequenceGeneratorTest {

    private final RoboticsSequenceGenerator generator = new RoboticsSequenceGenerator();

    @Test
    void nextSequenceReturnsOneWhenHistoryEmpty() {
        assertThat(generator.nextSequence(EventHistory.builder().build())).isEqualTo(1);
    }

    @Test
    void nextSequenceFindsMaxAcrossAllBuckets() {
        Event event1 = Event.builder().eventSequence(2).dateReceived(LocalDateTime.now()).build();
        Event event2 = Event.builder().eventSequence(5).dateReceived(LocalDateTime.now()).build();
        Event event3 = Event.builder().eventSequence(3).dateReceived(LocalDateTime.now()).build();

        EventHistory history = EventHistory.builder()
            .miscellaneous(List.of(event1))
            .defenceFiled(List.of(event2))
            .breathingSpaceEntered(List.of(event3))
            .build();

        assertThat(generator.nextSequence(history)).isEqualTo(6);
    }

    @Test
    void nextSequenceIgnoresNullEvents() {
        Event event = Event.builder().eventSequence(null).dateReceived(LocalDateTime.now()).build();

        EventHistory history = EventHistory.builder()
            .miscellaneous(List.of(event))
            .build();

        assertThat(generator.nextSequence(history)).isEqualTo(1);
    }

    @Test
    void nextSequenceSkipsNullSequencesAcrossLists() {
        Event nullSequence = Event.builder().eventSequence(null).dateReceived(LocalDateTime.now()).build();
        Event maxEvent = Event.builder().eventSequence(7).dateReceived(LocalDateTime.now()).build();

        EventHistory history = EventHistory.builder()
            .miscellaneous(List.of(nullSequence))
            .statesPaid(List.of(maxEvent))
            .build();

        assertThat(generator.nextSequence(history)).isEqualTo(8);
    }
}
