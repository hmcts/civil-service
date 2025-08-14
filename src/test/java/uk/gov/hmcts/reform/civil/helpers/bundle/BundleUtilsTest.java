package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundleUtilsTest {

    @Test
    void shouldGenerateDocNameWithOnlyDateWhenStrParamIsBlank() {

        String fileName = "Document_%s";
        String strParam = "";
        String strParam2 = "SomeText";
        LocalDate date = LocalDate.of(2023, 10, 5);

        String result = BundleUtils.generateDocName(fileName, strParam, strParam2, date);

        assertEquals("Document_05/10/2023", result);
    }

    @Test
    void shouldGenerateDocNameWithStrParamWhenStrParam2IsBlank() {

        String fileName = "Document_%s_%s";
        String strParam = "Param1";
        String strParam2 = "";
        LocalDate date = LocalDate.of(2023, 10, 5);

        String result = BundleUtils.generateDocName(fileName, strParam, strParam2, date);

        assertEquals("Document_Param1_05/10/2023", result);
    }

    @Test
    void shouldGenerateDocNameWithStrParamAndStrParam2WhenBothAreProvided() {
        // Given
        String fileName = "Document_%s_%s_%s";
        String strParam = "Param1";
        String strParam2 = "Param2";
        LocalDate date = LocalDate.of(2023, 10, 5);

        String result = BundleUtils.generateDocName(fileName, strParam, strParam2, date);

        assertEquals("Document_Param1_Param2_05/10/2023", result);
    }

    @Test
    void shouldBuildBundlingRequestDocWithGivenParameters() {

        String docName = "TestDocument";
        Document document = Document.builder()
            .documentUrl("http://example.com/document.pdf")
            .documentBinaryUrl("http://example.com/document-binary.pdf")
            .documentFileName("document.pdf")
            .build();
        String docType = "TestDocType";

        BundlingRequestDocument result = BundleUtils.buildBundlingRequestDoc(docName, document, docType);

        assertEquals(docName, result.getDocumentFileName());
        assertEquals(docType, result.getDocumentType());
        assertNotNull(result.getDocumentLink());
        assertEquals("http://example.com/document.pdf", result.getDocumentLink().getDocumentUrl());
        assertEquals("http://example.com/document-binary.pdf", result.getDocumentLink().getDocumentBinaryUrl());
        assertEquals("document.pdf", result.getDocumentLink().getDocumentFilename());
    }

    @Test
    void shouldTruncateDocNameWhenExceedsMaxLength() {
        String longParam = "A".repeat(300); // More than 255 characters
        String fileName = "%s";
        LocalDate date = LocalDate.of(2023, 10, 5);

        String result = BundleUtils.generateDocName(fileName, longParam, "Param2", date);

        assertEquals(BundleUtils.MAX_DOC_TITLE_LENGTH, result.length());
        assertEquals(longParam.substring(0, BundleUtils.MAX_DOC_TITLE_LENGTH), result);
    }

    @Test
    void shouldTruncateDocNameWhenDateAndParamsCauseOverflow() {
        String longParam1 = "X".repeat(200);
        String longParam2 = "Y".repeat(100); // Together > 255 chars
        String fileName = "%s_%s_%s";
        LocalDate date = LocalDate.of(2023, 10, 5);

        String result = BundleUtils.generateDocName(fileName, longParam1, longParam2, date);

        assertEquals(BundleUtils.MAX_DOC_TITLE_LENGTH, result.length());
        assertTrue(result.startsWith(longParam1.substring(0, 200))); // still starts with part of first param
    }
}
