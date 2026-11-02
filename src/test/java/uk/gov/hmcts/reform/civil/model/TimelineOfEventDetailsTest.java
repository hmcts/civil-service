package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TimelineOfEventDetailsTest {

    @Test
    void shouldReturnStoredTimelineDate_whenPresent() {
        LocalDate storedDate = LocalDate.of(2023, 6, 1);

        TimelineOfEventDetails details = new TimelineOfEventDetails(storedDate, "15", "3", "2024", "Something happened");

        assertThat(details.getTimelineDate()).isEqualTo(storedDate);
    }

    @Test
    void shouldBuildTimelineDateFromParts_whenDayMonthAndYearPresent() {
        TimelineOfEventDetails details = new TimelineOfEventDetails(null, "15", "3", "2024", "Something happened");

        assertThat(details.getTimelineDate()).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "NULL, 3, 2024",
        "'', 3, 2024",
        "15, NULL, 2024",
        "15, 3, NULL",
        "31, 2, 2024",
        "undefined, 3, 2024"
    }, nullValues = "NULL")
    void shouldReturnNoTimelineDate_whenPartsDoNotMakeAFullDate(String day, String month, String year) {
        TimelineOfEventDetails details = new TimelineOfEventDetails(null, day, month, year, "Something happened");

        assertThat(details.getTimelineDate()).isNull();
    }

    @Test
    void shouldSerialiseTimelineDateBuiltFromParts() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TimelineOfEventDetails details = new TimelineOfEventDetails(null, "15", "3", "2024", "Something happened");

        Map<?, ?> json = objectMapper.convertValue(details, Map.class);

        assertThat(json.get("timelineDate")).isEqualTo("2024-03-15");
    }
}
