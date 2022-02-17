package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdValueTest {

    private final String id = "1";
    private final Integer value = 1234;

    private IdValue<Integer> addressUk = new IdValue<>(
        id,
        value
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(id, addressUk.getId());
        assertEquals(value, addressUk.getValue());
    }
}
