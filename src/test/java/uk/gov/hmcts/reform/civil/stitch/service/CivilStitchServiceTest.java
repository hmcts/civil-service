package uk.gov.hmcts.reform.civil.stitch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DocumentConversionService;
import uk.gov.hmcts.reform.civil.stitch.PdfMerger;
import uk.gov.hmcts.reform.civil.stitch.PdfMergeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(MockitoExtension.class)
class CivilStitchServiceTest {

    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @InjectMocks
    private CivilStitchService civilStitchService;

    @Mock
    private DocumentManagementService managementService;
    @Mock
    private DocumentConversionService conversionService;

    MockedStatic<PdfMerger> pdfMergerMockedStatic;

    @Test
    void shouldReturnStitchedDocuments() throws Exception {
        byte[] docArray;
        try (var input = getClass().getClassLoader().getResourceAsStream("stitch-documents/test1.pdf")) {
            docArray = input.readAllBytes();
        }
        when(conversionService.convertDocumentToPdf(any(Document.class), anyLong(), anyString())).thenReturn(docArray);
        when(managementService.uploadDocument(anyString(), any(PDF.class))).thenReturn(STITCHED_DOC);
        pdfMergerMockedStatic = Mockito.mockStatic(PdfMerger.class);
        pdfMergerMockedStatic.when(() -> PdfMerger.mergeDocuments(anyList(), anyString())).thenReturn(docArray);

        CaseDocument caseDocument = civilStitchService.generateStitchedCaseDocument(documents,
                                                                                    "seal-form-000-DC-123.pdf",
                                                                                    1L,
                                                                                    DocumentType.SEALED_CLAIM,
                                                                                    BEARER_TOKEN);
        assertEquals(STITCHED_DOC, caseDocument);
        pdfMergerMockedStatic.close();
    }

    @Test
    void shouldIdentifyMalformedPdfAndPreserveParserCause() {
        byte[] malformedPdf = ("%PDF-1.4\n1 0 obj\n<< /Type /Pages /Kids [] /Count 0 >>\nendobj\n"
            + "trailer\n<< /Size 2 >>\nstartxref\n0\n%%EOF\n").getBytes(StandardCharsets.US_ASCII);

        assertInvalidDocument(malformedPdf, "http://cdam/documents/upload-id", "upload-id",
                              "Missing root object specification in trailer.");
    }

    @Test
    void shouldIdentifyInvalidDocumentWithoutUrl() {
        assertInvalidDocument("not a PDF".getBytes(StandardCharsets.US_ASCII), null, "unknown", null);
    }

    @Test
    void shouldIdentifyDocumentWhenConversionReturnsNull() {
        assertInvalidDocument(null, "http://cdam/documents/upload-id", "upload-id", null);
    }

    private void assertInvalidDocument(byte[] bytes, String url, String expectedId, String causeMessage) {
        Document document = new Document().setDocumentFileName("particulars.pdf").setDocumentUrl(url);
        when(conversionService.convertDocumentToPdf(document, 1L, BEARER_TOKEN)).thenReturn(bytes);

        var assertion = assertThatThrownBy(() -> civilStitchService.generateStitchedCaseDocument(
            List.of(new DocumentMetaData(document, "Particulars", "2026-10-30")),
            "sealed-claim.pdf", 1L, SEALED_CLAIM, BEARER_TOKEN
        ))
            .isInstanceOf(PdfMergeException.class)
            .hasMessage("Unable to parse PDF document 'particulars.pdf' (CDAM id: " + expectedId + ") for caseId 1")
            .hasCauseInstanceOf(IOException.class);
        if (causeMessage != null) {
            assertion.hasRootCauseMessage(causeMessage);
        }
        verifyNoInteractions(managementService);
    }

    private final List<DocumentMetaData> documents = Arrays.asList(
        new DocumentMetaData(
            CLAIM_FORM.getDocumentLink(),
            "Sealed Claim Form",
            LocalDate.now().toString()
        ),
        new DocumentMetaData(
            LIP_FORM.getDocumentLink(),
            "Litigant in person claim form",
            LocalDate.now().toString()
        )
    );

    private static final CaseDocument CLAIM_FORM =
        new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .setDocumentSize(0L)
            .setDocumentType(SEALED_CLAIM)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));

    private static final CaseDocument LIP_FORM =
        new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000DC001"))
            .setDocumentSize(0L)
            .setDocumentType(LITIGANT_IN_PERSON_CLAIM_FORM)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));

    private static final CaseDocument STITCHED_DOC =
        new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("Stitched document")
            .setDocumentSize(0L)
            .setDocumentType(SEALED_CLAIM)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));

}
