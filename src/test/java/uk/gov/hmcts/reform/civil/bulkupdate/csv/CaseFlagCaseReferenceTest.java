package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseFlagCaseReferenceTest {

    @Test
    void shouldMapExcelRow() {
        CaseFlagCaseReference reference = new CaseFlagCaseReference();

        reference.fromExcelRow(Map.of(
            "caseReference", "1234567890123456",
            "comment", "Migration flag comment"
        ));

        assertThat(reference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(reference.getComment()).isEqualTo("Migration flag comment");
    }

    @Test
    void shouldMapMissingValuesToNull() {
        Map<String, Object> row = new HashMap<>();
        row.put("caseReference", null);
        row.put("comment", null);
        CaseFlagCaseReference reference = new CaseFlagCaseReference();

        reference.fromExcelRow(row);

        assertThat(reference.getCaseReference()).isNull();
        assertThat(reference.getComment()).isNull();
    }
}
