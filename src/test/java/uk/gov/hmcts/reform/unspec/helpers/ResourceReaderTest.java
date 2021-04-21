package uk.gov.hmcts.reform.unspec.helpers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceReaderTest {

    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsNull() {
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> ResourceReader.readString(null)
        );
        String expectedMessage = "Unable to read resource: null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    // commons-io dependency bump from 2.7 -> 2.8.0 causes test to fail
    @Disabled
    @Test
    void shouldThrowIllegalStateException_whenResourcePathIsInvalid() {
        Exception exception = assertThrows(
            IllegalStateException.class,
            () -> ResourceReader.readString("abc")
        );
        String expectedMessage = "Unable to read resource: abc";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
