package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UnavailableDateTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Test
    void shouldTreatMalformedUnavailableDatesAsNull() throws Exception {
        String json = """
            {
              "date": "--28",
              "fromDate": "bad-from-date",
              "toDate": "2026-13-40",
              "dateAdded": "not-a-date",
              "unavailableDateType": "SINGLE_DATE"
            }
            """;

        UnavailableDate unavailableDate = objectMapper.readValue(json, UnavailableDate.class);

        assertThat(unavailableDate.getDate()).isNull();
        assertThat(unavailableDate.getFromDate()).isNull();
        assertThat(unavailableDate.getToDate()).isNull();
        assertThat(unavailableDate.getDateAdded()).isNull();
        assertThat(unavailableDate.getUnavailableDateType()).isEqualTo(UnavailableDateType.SINGLE_DATE);
    }

    @Test
    void shouldDeserializeValidIsoUnavailableDates() throws Exception {
        String json = """
            {
              "date": "2026-08-21",
              "fromDate": "2026-08-22",
              "toDate": "2026-08-23",
              "dateAdded": "2026-08-20",
              "unavailableDateType": "DATE_RANGE"
            }
            """;

        UnavailableDate unavailableDate = objectMapper.readValue(json, UnavailableDate.class);

        assertThat(unavailableDate.getDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(unavailableDate.getFromDate()).isEqualTo(LocalDate.of(2026, 8, 22));
        assertThat(unavailableDate.getToDate()).isEqualTo(LocalDate.of(2026, 8, 23));
        assertThat(unavailableDate.getDateAdded()).isEqualTo(LocalDate.of(2026, 8, 20));
        assertThat(unavailableDate.getUnavailableDateType()).isEqualTo(UnavailableDateType.DATE_RANGE);
    }
}
