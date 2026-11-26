package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventHistoryTest {

    @Test
    void shouldSerialiseUnsetEventListsAsEmptyArraysNotNull() {
        JsonNode json = new ObjectMapper().valueToTree(new EventHistory());

        assertThat(json.properties()).isNotEmpty();
        json.properties().forEach(field ->
            assertThat(field.getValue().isArray())
                .as("events.%s should be an array", field.getKey())
                .isTrue());
    }
}
