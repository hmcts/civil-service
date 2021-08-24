package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
    EventHistorySequencer.class
})
class EventHistorySequencerTest {

    @Autowired
    EventHistorySequencer eventHistorySequencer;

    @Test
    void shouldAddSequence_whenSingleEventInEventHistory() {
        Event event = Event.builder()
            .eventCode("999")
            .dateReceived(LocalDateTime.now())
            .eventDetailsText("RPA Reason: Some event Happened.")
            .eventDetails(EventDetails.builder()
                              .miscText("RPA Reason: Some event Happened.")
                              .build())
            .build();
        EventHistory eventHistory = EventHistory.builder()
            .miscellaneous(List.of(event))
            .build();

        var result = eventHistorySequencer.sortEvents(eventHistory);
        assertThat(result).isNotNull();
        assertThat(result)
            .extracting(EventHistory::getMiscellaneous)
            .isEqualTo(List.of(event.toBuilder().eventSequence(1).build()));
    }

    @Test
    void shouldNotSort_whenNoEventInEventHistory() {
        EventHistory eventHistory = EventHistory.builder().build();
        var result = eventHistorySequencer.sortEvents(eventHistory);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(eventHistory);
    }

    @Test
    void shouldThrowException_whenPassedNullObject() {
        assertThrows(
            NullPointerException.class,
            () -> eventHistorySequencer.sortEvents(null)
        );
    }
}
