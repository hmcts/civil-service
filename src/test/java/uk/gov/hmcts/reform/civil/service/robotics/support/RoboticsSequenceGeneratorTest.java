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
        assertThat(generator.nextSequence(new EventHistory())).isEqualTo(1);
    }

    @Test
    void nextSequenceFindsMaxAcrossAllBuckets() {
        Event event1 = new Event();
        event1.setEventSequence(2);
        event1.setDateReceived(LocalDateTime.now());
        Event event2 = new Event();
        event2.setEventSequence(5);
        event2.setDateReceived(LocalDateTime.now());
        Event event3 = new Event();
        event3.setEventSequence(3);
        event3.setDateReceived(LocalDateTime.now());

        EventHistory history = new EventHistory();
        history.setMiscellaneous(List.of(event1));
        history.setDefenceFiled(List.of(event2));
        history.setBreathingSpaceEntered(List.of(event3));

        assertThat(generator.nextSequence(history)).isEqualTo(6);
    }

    @Test
    void nextSequenceIgnoresNullEvents() {
        Event event = new Event();
        event.setEventSequence(null);
        event.setDateReceived(LocalDateTime.now());

        EventHistory history = new EventHistory();
        history.setMiscellaneous(List.of(event));

        assertThat(generator.nextSequence(history)).isEqualTo(1);
    }

    @Test
    void nextSequenceSkipsNullSequencesAcrossLists() {
        Event nullSequence = new Event();
        nullSequence.setEventSequence(null);
        nullSequence.setDateReceived(LocalDateTime.now());
        Event maxEvent = new Event();
        maxEvent.setEventSequence(7);
        maxEvent.setDateReceived(LocalDateTime.now());

        EventHistory history = new EventHistory();
        history.setMiscellaneous(List.of(nullSequence));
        history.setStatesPaid(List.of(maxEvent));

        assertThat(generator.nextSequence(history)).isEqualTo(8);
    }
}
