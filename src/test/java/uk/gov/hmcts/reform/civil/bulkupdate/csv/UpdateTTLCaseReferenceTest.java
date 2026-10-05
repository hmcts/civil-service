package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTTLCaseReferenceTest {

    @Test
    void shouldPopulateCaseReferenceAndSystemTtlFromExcelRow() throws Exception {
        UpdateTTLCaseReference reference = new UpdateTTLCaseReference();

        reference.fromExcelRow(Map.of(
            "caseReference", 1234567890123456L,
            "ttl", "2032-09-08"
        ));

        assertThat(reference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(reference.getSystemTTL()).isEqualTo("2032-09-08");
    }

    @Test
    void shouldMapExplicitNullValues() throws Exception {
        UpdateTTLCaseReference reference = new UpdateTTLCaseReference();
        Map<String, Object> row = new HashMap<>();
        row.put("caseReference", null);
        row.put("ttl", null);

        reference.fromExcelRow(row);

        assertThat(reference.getCaseReference()).isNull();
        assertThat(reference.getSystemTTL()).isNull();
    }

    @Test
    void shouldIgnoreMissingColumns() throws Exception {
        UpdateTTLCaseReference reference = new UpdateTTLCaseReference();

        reference.fromExcelRow(Map.of());

        assertThat(reference.getCaseReference()).isNull();
        assertThat(reference.getSystemTTL()).isNull();
    }
}
