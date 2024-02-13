package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CaseEventTest {

    @Test
    public void testFromStringValid() {
        // Test valid inputs
        assertEquals(CaseEvent.CREATE_CLAIM, CaseEvent.fromString("CREATE_CLAIM"));
        assertEquals(CaseEvent.CREATE_CLAIM_SPEC, CaseEvent.fromString("CREATE_CLAIM_SPEC"));
        assertEquals(CaseEvent.CREATE_SERVICE_REQUEST_CLAIM, CaseEvent.fromString("CREATE_SERVICE_REQUEST_CLAIM"));
        assertEquals(
            CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT,
            CaseEvent.fromString("CREATE_CLAIM_SPEC_AFTER_PAYMENT")
        );
    }

    @Test
    public void testFromStringInvalid() {
        // Test invalid input
        assertThrows(IllegalArgumentException.class, () -> CaseEvent.fromString("INVALID_EVENT"));
    }
}
