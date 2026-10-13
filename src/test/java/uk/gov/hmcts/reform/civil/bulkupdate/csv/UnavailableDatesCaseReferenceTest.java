package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnavailableDatesCaseReferenceTest {

    @Test
    void shouldMapExcelRow() {
        Map<String, Object> row = Map.of(
            "caseReference", "1234567890123456",
            "partyType", "Defendant",
            "unavailableDateType", "DATE_RANGE",
            "fromDate", "2026-09-07",
            "toDate", "2026-09-14"
        );

        UnavailableDatesCaseReference reference = new UnavailableDatesCaseReference();

        reference.fromExcelRow(row);

        assertThat(reference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(reference.getPartyType()).isEqualTo("Defendant");
        assertThat(reference.getUnavailableDateType()).isEqualTo("DATE_RANGE");
        assertThat(reference.getFromDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(reference.getToDate()).isEqualTo(LocalDate.of(2026, 9, 14));
    }

    @Test
    void shouldMapMissingValuesToNull() {
        Map<String, Object> row = new HashMap<>();
        row.put("caseReference", null);
        row.put("partyType", null);
        row.put("unavailableDateType", null);
        row.put("fromDate", null);
        row.put("toDate", null);

        UnavailableDatesCaseReference reference = new UnavailableDatesCaseReference();

        reference.fromExcelRow(row);

        assertThat(reference.getCaseReference()).isNull();
        assertThat(reference.getPartyType()).isNull();
        assertThat(reference.getUnavailableDateType()).isNull();
        assertThat(reference.getFromDate()).isNull();
        assertThat(reference.getToDate()).isNull();
    }
}
