package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelCaseReferenceTest {

    @Test
    void shouldSetCaseReferenceWhenPresentInRow() throws Exception {
        ExcelCaseReference excelCaseReference = new ExcelCaseReference();
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "1234567890123456");

        excelCaseReference.fromExcelRow(rowValues);

        assertThat(excelCaseReference.getCaseReference()).isEqualTo("1234567890123456");
    }

    @Test
    void shouldSetCaseReferenceToNullWhenValueIsNull() throws Exception {
        ExcelCaseReference excelCaseReference = new ExcelCaseReference();
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", null);

        excelCaseReference.fromExcelRow(rowValues);

        assertThat(excelCaseReference.getCaseReference()).isNull();
    }

    @Test
    void shouldNotSetCaseReferenceWhenKeyIsMissing() throws Exception {
        ExcelCaseReference excelCaseReference = new ExcelCaseReference();
        Map<String, Object> rowValues = new HashMap<>();

        excelCaseReference.fromExcelRow(rowValues);

        assertThat(excelCaseReference.getCaseReference()).isNull();
    }
}
