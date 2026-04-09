package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import javax.crypto.SecretKey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseReferenceCsvLoaderTest {

    CaseReferenceCsvLoader caseReferenceCsvLoader = new CaseReferenceCsvLoader();

    static class TestExcelCaseReference extends CaseReference implements ExcelMappable {
        public String caseId;
        public String description;

        @Override
        public void fromExcelRow(java.util.Map<String, Object> rowValues) {
            this.caseId = rowValues.get("caseId").toString();
            this.description = rowValues.get("description").toString();
        }
    }

    @Test
    void shouldLoadCaseRefsCsvFile() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, "caserefs-test.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(caseReferences.size(), equalTo(4));
    }

    @Test
    void shouldReturnEmptyCollectionWhenFileNotFound() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, "caserefs-test-file-does-not-exist.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.TRUE));
    }

    @Test
    void testEncryptDecrypt() throws Exception {
        String originalString = "This is a test string";
        String secret = "mySecretKey";

        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(secret);
        String encryptedString = CaseReferenceCsvLoader.encrypt(originalString, key);
        String decryptedString = CaseReferenceCsvLoader.decrypt(encryptedString, key);

        assertEquals(originalString, decryptedString);
    }

    @Test
    void shouldLoadEncryptedCaseRefsCsvFile() {
        String secret = "DUMMY_SECRET";
        String fileName = "caserefs-test-encrypted.csv";

        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, fileName, secret);

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertEquals(3, caseReferences.size());
    }

    @Test
    void testGetKeyFromString() throws Exception {
        String secret = "mySecretKey";
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(secret);

        assertNotNull(key);
        assertArrayEquals(CaseReferenceCsvLoader.getKeyFromString(secret).getEncoded(), key.getEncoded());
    }

    @Test
    void shouldReturnEmptyListForEncryptedCsvFileWithIncorrectSecret() {
        String secret = "incorrect-secret";
        String fileName = "caserefs-test-encrypted.csv";

        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, fileName, secret);

        Assertions.assertThat(caseReferences).isEmpty();
    }

    @Test
    void shouldThrowExceptionOnInvalidCsvFile() {
        String secret = "invalid-secret";
        String fileName = "invalid-csv-file.csv";

        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(CaseReference.class, fileName, secret);

        Assertions.assertThat(caseReferences).isEmpty();
    }

    @Test
    void shouldLoadFromExcelBytes() throws Exception {

        // Create an in-memory Excel file
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            var sheet = workbook.createSheet();
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("caseId");
            header.createCell(1).setCellValue("description");

            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("12345");
            row.createCell(1).setCellValue("Test Case");

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            workbook.write(bos);
            byte[] excelBytes = bos.toByteArray();

            List<TestExcelCaseReference> list =
                caseReferenceCsvLoader.loadFromExcelBytes(TestExcelCaseReference.class, excelBytes);

            assertNotNull(list);
            assertEquals(1, list.size());
            assertEquals("12345", list.get(0).caseId);
            assertEquals("Test Case", list.get(0).description);
        }
    }
}
