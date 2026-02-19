package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RoboticsTimelineHelperTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2023, 6, 15, 10, 0);

    private final Time time = Mockito.mock(Time.class);
    private RoboticsTimelineHelper helper;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(FIXED_NOW);
        helper = new RoboticsTimelineHelper(time);
    }

    @Test
    void ensurePresentOrNowReturnsCandidateWhenFuture() {
        LocalDateTime future = FIXED_NOW.plusDays(1);

        assertThat(helper.ensurePresentOrNow(future)).isEqualTo(future);
    }

    @Test
    void ensurePresentOrNowFallsBackToNowWhenNull() {
        assertThat(helper.ensurePresentOrNow(null)).isEqualTo(FIXED_NOW);
    }

    @Test
    void ensurePresentOrNowFallsBackWhenPast() {
        LocalDateTime past = FIXED_NOW.minusMinutes(5);

        assertThat(helper.ensurePresentOrNow(past)).isEqualTo(FIXED_NOW);
    }

    @Test
    void withFallbackReturnsCandidateWhenPresent() {
        String candidate = "value";

        String resolved = helper.withFallback(candidate, () -> "fallback");

        assertThat(resolved).isEqualTo("value");
    }

    @Test
    void withFallbackUsesSupplierWhenMissing() {
        String resolved = helper.withFallback(null, () -> "fallback");

        assertThat(resolved).isEqualTo("fallback");
    }

    @Test
    void toIsoDateFormatsAsExpected() {
        LocalDate date = LocalDate.of(2024, 2, 29);

        assertThat(helper.toIsoDate(date)).isEqualTo("2024-02-29");
    }

    @Test
    void toIsoDateReturnsNullWhenMissing() {
        assertThat(helper.toIsoDate(null)).isNull();
    }

    @Test
    void toIsoDateTimeFormatsAsExpected() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 5, 14, 30);

        assertThat(helper.toIsoDateTime(dateTime)).isEqualTo("2024-01-05T14:30:00");
    }

    @Test
    void toIsoDateTimeReturnsNullWhenMissing() {
        assertThat(helper.toIsoDateTime(null)).isNull();
    }

    @Test
    void nowDelegatesToTimeBean() {
        assertThat(helper.now()).isEqualTo(FIXED_NOW);
        Mockito.verify(time).now();
    }
}
