package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
    EventHistorySequencer.class
})
class EventHistorySequencerTest {

    @Autowired
    EventHistorySequencer eventHistorySequencer;

    @Test
    void shouldThrowException_whenPassedNullObject() {
        assertThrows(
            NullPointerException.class,
            () -> eventHistorySequencer.sortEvents(null)
        );
    }
}
