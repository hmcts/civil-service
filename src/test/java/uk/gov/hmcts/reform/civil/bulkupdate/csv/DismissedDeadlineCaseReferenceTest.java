package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DismissedDeadlineCaseReferenceTest {

    private DismissedDeadlineCaseReference caseReference;

    @BeforeEach
    void setUp() {
        caseReference = new DismissedDeadlineCaseReference();
    }

    @Test
    void shouldPopulateFieldsFromExcelRow() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "1234567890123456");
        rowValues.put("dismissedDeadline", "2026-07-17T10:15:30");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDismissedDeadline()).isEqualTo("2026-07-17T10:15:30");
    }

    @Test
    void shouldConvertNonStringValuesFromExcelRow() throws Exception {
        LocalDateTime dismissedDeadline = LocalDateTime.of(2026, 7, 17, 10, 15, 30);
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", 1234567890123456L);
        rowValues.put("dismissedDeadline", dismissedDeadline);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDismissedDeadline()).isEqualTo("2026-07-17T10:15:30");
    }

    @Test
    void shouldSetFieldsToNullWhenValuesAreNull() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", null);
        rowValues.put("dismissedDeadline", null);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isNull();
        assertThat(caseReference.getDismissedDeadline()).isNull();
    }

    @Test
    void shouldIgnoreMissingFields() throws Exception {
        caseReference.setCaseReference("1234567890123456");
        caseReference.setDismissedDeadline("2026-07-17T10:15:30");

        caseReference.fromExcelRow(Map.of());

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDismissedDeadline()).isEqualTo("2026-07-17T10:15:30");
    }
}
