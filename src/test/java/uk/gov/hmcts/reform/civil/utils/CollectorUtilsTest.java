package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.utils.CollectorUtils.toSingleton;

public class CollectorUtilsTest {

    private List<String> list;

    @BeforeEach
    void setUp() {
        list = List.of("value", "two");
    }

    @Test
    void shouldCreateObject_whenSingleObjectList() {
        String actual = list.stream().filter(s -> s.contains("v")).collect(toSingleton());

        assertThat(actual).isEqualTo("value");
    }

    @Test
    void shouldThrowError_whenListHasMoreThanOneObject() {
        assertThrows(
            IllegalStateException.class,
            () -> list.stream().collect(toSingleton()));
    }
}
