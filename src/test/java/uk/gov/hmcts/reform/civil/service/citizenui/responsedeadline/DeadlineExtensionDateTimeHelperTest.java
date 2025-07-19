package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class DeadlineExtensionDateTimeHelperTest {

    private final DeadlineExtensionDateTimeHelper helper = new DeadlineExtensionDateTimeHelper();

    @Test
    void shouldCombineGivenDateWithCurrentLondonTime() {
        // Arrange
        LocalDate givenDate = LocalDate.of(2023, 11, 15);
        ZonedDateTime nowInLondon = ZonedDateTime.now(ZoneId.of("Europe/London"));
        LocalTime currentLondonTime = nowInLondon.toLocalTime();

        // Act
        LocalDateTime result = helper.createDateTimeWithNowTime(givenDate);

        // Assert
        assertThat(result).isCloseTo(LocalDateTime.of(givenDate, currentLondonTime),
            within(1, java.time.temporal.ChronoUnit.SECONDS));
    }
}
