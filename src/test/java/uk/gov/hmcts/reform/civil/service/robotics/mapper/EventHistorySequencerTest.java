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
    void shouldSortSequenceBasedOnDateReceived_whenEventHistoryProvided() {
        Event event = eventBuilder()
            .eventCode("999")
            .eventDetailsText("RPA Reason: Some event Happened.")
            .eventDetails(new EventDetails()
                              .setMiscText("RPA Reason: Some event Happened.")
                              )
            .build();
        LocalDateTime now = LocalDateTime.now();
        Event firstEvent = eventBuilderFrom(event)
            .eventSequence(3)
            .dateReceived(now.minusDays(2))
            .build();
        Event secondEvent = eventBuilderFrom(event)
            .eventSequence(1)
            .dateReceived(now)
            .build();
        Event thirdEvent = eventBuilderFrom(event)
            .eventSequence(2)
            .dateReceived(now.plusDays(5))
            .build();
        EventHistory eventHistory = new EventHistory()
            .setMiscellaneous(List.of(firstEvent, secondEvent, thirdEvent));

        var result = eventHistorySequencer.sortEvents(eventHistory);
        assertThat(result).isNotNull();
        assertThat(result)
            .extracting(EventHistory::getMiscellaneous)
            .isEqualTo(List.of(
                eventBuilderFrom(firstEvent).eventSequence(1).build(),
                eventBuilderFrom(secondEvent).eventSequence(2).build(),
                eventBuilderFrom(thirdEvent).eventSequence(3).build()
            ));
    }

    @Test
    void shouldAddSequence_whenSingleEventInEventHistory() {
        Event event = eventBuilder()
            .eventCode("999")
            .dateReceived(LocalDateTime.now())
            .eventDetailsText("RPA Reason: Some event Happened.")
            .eventDetails(new EventDetails()
                              .setMiscText("RPA Reason: Some event Happened.")
                              )
            .build();
        EventHistory eventHistory = new EventHistory()
            .setMiscellaneous(List.of(event));

        var result = eventHistorySequencer.sortEvents(eventHistory);
        assertThat(result).isNotNull();
        assertThat(result)
            .extracting(EventHistory::getMiscellaneous)
            .isEqualTo(List.of(eventBuilderFrom(event).eventSequence(1).build()));
    }

    @Test
    void shouldThrowException_whenPassedNullObject() {
        assertThrows(
            NullPointerException.class,
            () -> eventHistorySequencer.sortEvents(null)
        );
    }

    private EventTestBuilder eventBuilder() {
        return new EventTestBuilder();
    }

    private EventTestBuilder eventBuilderFrom(Event event) {
        return new EventTestBuilder(event);
    }

    private static class EventTestBuilder {
        private final Event event;

        EventTestBuilder() {
            this.event = new Event();
        }

        EventTestBuilder(Event existing) {
            this();
            event.setEventSequence(existing.getEventSequence());
            event.setEventCode(existing.getEventCode());
            event.setDateReceived(existing.getDateReceived());
            event.setLitigiousPartyID(existing.getLitigiousPartyID());
            event.setEventDetailsText(existing.getEventDetailsText());
            event.setEventDetails(existing.getEventDetails());
        }

        EventTestBuilder eventSequence(Integer sequence) {
            event.setEventSequence(sequence);
            return this;
        }

        EventTestBuilder eventCode(String code) {
            event.setEventCode(code);
            return this;
        }

        EventTestBuilder dateReceived(LocalDateTime dateReceived) {
            event.setDateReceived(dateReceived);
            return this;
        }

        EventTestBuilder litigiousPartyID(String id) {
            event.setLitigiousPartyID(id);
            return this;
        }

        EventTestBuilder eventDetails(EventDetails details) {
            event.setEventDetails(details);
            return this;
        }

        EventTestBuilder eventDetailsText(String text) {
            event.setEventDetailsText(text);
            return this;
        }

        Event build() {
            return event;
        }
    }
}
