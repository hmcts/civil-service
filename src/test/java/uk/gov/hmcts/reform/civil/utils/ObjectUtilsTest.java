package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.fifthNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.firstNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.fourthNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.secondNonNull;
import static uk.gov.hmcts.reform.civil.utils.ObjectUtils.thirdNonNull;

class ObjectUtilsTest {

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        Constructor<ObjectUtils> constructor = ObjectUtils.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void shouldReturnNull_whenNonNullDoesNotExist() {
        assertNull(firstNonNull(null, null, null));
    }

    @Test
    void shouldReturnNull_whenNullArrayPassed() {
        Arrays.asList(
            null,
            null,
            null,
            null,
            null
        ).forEach(Assertions::assertNull);
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

    @Test
    void shouldReturnNull_whenNotEnoughNonNullValues() {
        assertNull(secondNonNull(null, "value1"));
        assertNull(thirdNonNull(null, "value1", "value2"));
        assertNull(fourthNonNull(null, "value1", "value2", "value3"));
        assertNull(fifthNonNull(null, "value1", "value2", "value3", "value4"));
    }

    @Test
    void shouldReturnFirstValue_whenAllNonNull() {
        assertEquals("value1", firstNonNull("value1", "value2", "value3"));
    }
}
