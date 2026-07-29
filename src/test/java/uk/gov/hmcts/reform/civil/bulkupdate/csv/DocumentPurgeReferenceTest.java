package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPurgeReferenceTest {

    private DocumentPurgeReference caseReference;

    @BeforeEach
    void setUp() {
        caseReference = new DocumentPurgeReference();
    }

    @Test
    void shouldPopulateFieldsFromExcelRow() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "1234567890123456");
        rowValues.put("documentId", "6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDocumentId()).isEqualTo("6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");
    }

    @Test
    void shouldConvertNonStringValuesFromExcelRow() throws Exception {
        UUID documentId = UUID.fromString("6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", 1234567890123456L);
        rowValues.put("documentId", documentId);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDocumentId()).isEqualTo("6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");
    }

    @Test
    void shouldSetFieldsToNullWhenValuesAreNull() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", null);
        rowValues.put("documentId", null);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isNull();
        assertThat(caseReference.getDocumentId()).isNull();
    }

    @Test
    void shouldIgnoreMissingFields() throws Exception {
        caseReference.setCaseReference("1234567890123456");
        caseReference.setDocumentId("6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");

        caseReference.fromExcelRow(Map.of());

        assertThat(caseReference.getCaseReference()).isEqualTo("1234567890123456");
        assertThat(caseReference.getDocumentId()).isEqualTo("6c6403c1-ad61-4c60-9ffa-852ea0b25a7e");
    }
}
