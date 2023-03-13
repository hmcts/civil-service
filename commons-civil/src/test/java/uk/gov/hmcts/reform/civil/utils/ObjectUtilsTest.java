package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.fifthNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.firstNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.fourthNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.secondNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.thirdNonNull;

class ObjectUtilsTest {

    @Test
    void shouldReturnNull_whenNonNullDoesNotExist() {
        assertNull(firstNonNull(null, null, null));
    }

    @Test
    void shouldReturnFirstNonNull_whenProvided() {
        assertEquals(
            "value1",
            firstNonNull(
                null,
                null,
                "value1",
                "value2",
                "value3",
                "value4",
                "value5"
            )
        );
    }

    @Test
    void shouldReturnSecondNonNull_whenProvided() {
        assertEquals(
            "value2",
            secondNonNull(
                null,
                "value1",
                "value2",
                "value3",
                "value4",
                null,
                "value5"
            )
        );
    }

    @Test
    void shouldReturnThirdNonNull_whenProvided() {
        assertEquals(
            "value3",
            thirdNonNull(
                null,
                "value1",
                "value2",
                null,
                "value3",
                "value4",
                "value5"
            )
        );
    }

    @Test
    void shouldReturnFourthNonNull_whenProvided() {
        assertEquals(
            "value4",
            fourthNonNull(
                null,
                "value1",
                "value2",
                null,
                "value3",
                "value4",
                "value5"
            )
        );
    }

    @Test
    void shouldReturnFifthNonNull_whenProvided() {
        assertEquals(
            "value5",
            fifthNonNull(
                null,
                "value1",
                "value2",
                null,
                "value3",
                "value4",
                "value5"
            )
        );
    }
}
