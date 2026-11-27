package uk.gov.hmcts.reform.civil.stitch.service;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DocumentConversionService;
import uk.gov.hmcts.reform.civil.stitch.PdfMergeException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CivilStitchServiceIT {

    private static final String AUTHORISATION = "Bearer token";
    private static final long CASE_ID = 1776674103714442L;

    @Test
    void shouldNotUploadStitchedDocumentWhenDownloadedDocumentIsNotPdf() throws Exception {
        DocumentManagementService managementService = mock(DocumentManagementService.class);
        DocumentConversionService conversionService = mock(DocumentConversionService.class);
        CivilStitchService stitchService = new CivilStitchService(managementService, conversionService);
        Document sealedClaim = document("sealed-claim.pdf", "sealed-claim-url");
        Document supportedDocument = document("supported-document.pdf", "supported-document-url");
        byte[] validPdf = loadResource("stitch-documents/test1.pdf");

        when(conversionService.convertDocumentToPdf(sealedClaim, CASE_ID, AUTHORISATION)).thenReturn(validPdf);
        when(conversionService.convertDocumentToPdf(supportedDocument, CASE_ID, AUTHORISATION))
            .thenReturn("upstream error response".getBytes());

        List<DocumentMetaData> documents = List.of(
            metadata(sealedClaim, "Sealed Claim form"),
            metadata(supportedDocument, "Supported docs")
        );

        assertThatThrownBy(() -> stitchService.generateStitchedCaseDocument(
            documents,
            "sealed_claim_form.pdf",
            CASE_ID,
            DocumentType.SEALED_CLAIM,
            AUTHORISATION
        ))
            .isInstanceOf(PdfMergeException.class)
            .hasMessage(
                "Unable to parse PDF document 'supported-document.pdf' (CDAM id: supported-document-url)"
                    + " for caseId 1776674103714442"
            );

        verify(managementService, never()).uploadDocument(anyString(), any(PDF.class));
    }

    @Test
    void shouldIdentifyPdfWithMissingRootAndPreserveCause() throws Exception {
        DocumentManagementService managementService = mock(DocumentManagementService.class);
        DocumentConversionService conversionService = mock(DocumentConversionService.class);
        CivilStitchService stitchService = new CivilStitchService(managementService, conversionService);
        Document sealedClaim = document("sealed-claim.pdf", "http://cdam/documents/sealed-id");
        Document upload = document("particulars.pdf", "http://cdam/documents/upload-id");
        // No catalog exists for PDFBox to recover, despite the valid PDF header.
        byte[] malformedPdf = ("%PDF-1.4\n1 0 obj\n<< /Type /Pages /Kids [] /Count 0 >>\nendobj\n"
            + "trailer\n<< /Size 2 >>\nstartxref\n0\n%%EOF\n").getBytes(StandardCharsets.US_ASCII);
        assertThatThrownBy(() -> Loader.loadPDF(malformedPdf))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Missing root object specification in trailer");
        when(conversionService.convertDocumentToPdf(sealedClaim, CASE_ID, AUTHORISATION))
            .thenReturn(loadResource("stitch-documents/test1.pdf"));
        when(conversionService.convertDocumentToPdf(upload, CASE_ID, AUTHORISATION)).thenReturn(malformedPdf);

        assertThatThrownBy(() -> stitchService.generateStitchedCaseDocument(
            List.of(metadata(sealedClaim, "Sealed claim"), metadata(upload, "Particulars")),
            "sealed_claim_form.pdf", CASE_ID, DocumentType.SEALED_CLAIM, AUTHORISATION
        ))
            .isInstanceOf(PdfMergeException.class)
            .hasMessage("Unable to parse PDF document 'particulars.pdf' (CDAM id: upload-id)"
                            + " for caseId 1776674103714442")
            .hasCauseInstanceOf(IOException.class)
            .hasRootCauseMessage("Missing root object specification in trailer.");

        verify(managementService, never()).uploadDocument(anyString(), any(PDF.class));
    }

    private static Document document(String filename, String url) {
        return new Document().setDocumentFileName(filename).setDocumentUrl(url);
    }

    private static DocumentMetaData metadata(Document document, String description) {
        return new DocumentMetaData(document, description, LocalDate.now().toString());
    }

    private static byte[] loadResource(String filePath) throws Exception {
        URL url = ResourceLoader.class.getClassLoader().getResource(filePath);
        if (url == null) {
            throw new IllegalArgumentException("Could not find resource in path " + filePath);
        }
        return Files.readAllBytes(Paths.get(url.toURI()));
    }
}
