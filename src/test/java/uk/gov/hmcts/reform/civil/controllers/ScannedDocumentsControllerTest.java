package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScannedDocumentsControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CASE_ID = "1594901956117591";
    private static final long CASE_ID_LONG = Long.parseLong(CASE_ID);

    private MockMvc mockMvc;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private DocumentManagementService documentManagementService;

    private ScannedDocumentsController controller;

    @BeforeEach
    void setUp() {
        controller = new ScannedDocumentsController(coreCaseDataService, caseDetailsConverter, documentManagementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnScannedDocumentPdf() throws Exception {
        byte[] expectedPdf = "pdf content".getBytes();
        ScannedDocument scannedDocument = scannedDocumentWithSubtype("OCON9x");
        CaseData caseData = caseDataWithDocuments(scannedDocument);
        CaseDetails caseDetails = caseDetails();

        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(documentManagementService.downloadDocument(eq(AUTH_TOKEN), eq("http://dm-store/documents/doc-123")))
            .thenReturn(expectedPdf);

        mockMvc.perform(get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(content().bytes(expectedPdf));
    }

    @Test
    void shouldReturnScannedDocumentPdfWhenSubtypeComesFromFormSubtype() throws Exception {
        byte[] expectedPdf = "pdf content".getBytes();
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .formSubtype("OCON9x")
            .url(document())
            .build();
        CaseData caseData = caseDataWithDocuments(scannedDocument);
        CaseDetails caseDetails = caseDetails();

        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(documentManagementService.downloadDocument(eq(AUTH_TOKEN), eq("http://dm-store/documents/doc-123")))
            .thenReturn(expectedPdf);

        mockMvc.perform(get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(content().bytes(expectedPdf));
    }

    @Test
    void shouldReturnScannedDocumentPdfWhenSubtypeIsUnknownEnumValue() throws Exception {
        byte[] expectedPdf = "pdf content".getBytes();
        String rawSubtype = "customSubtype";
        ScannedDocument scannedDocument = scannedDocumentWithSubtype("CUSTOMSUBTYPE");
        CaseData caseData = caseDataWithDocuments(scannedDocument);
        CaseDetails caseDetails = caseDetails();

        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(documentManagementService.downloadDocument(eq(AUTH_TOKEN), eq("http://dm-store/documents/doc-123")))
            .thenReturn(expectedPdf);

        mockMvc.perform(get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", rawSubtype)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().bytes(expectedPdf));
    }

    @Test
    void shouldReturnServerErrorWhenCaseReferenceIsInvalid() {
        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", "not-a-number", "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid case reference: not-a-number");

        verifyNoInteractions(coreCaseDataService, caseDetailsConverter, documentManagementService);
    }

    @Test
    void shouldReturnServerErrorWhenCaseIsNotFound() {
        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(null);

        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Case not found for id: " + CASE_ID);
    }

    @Test
    void shouldReturnServerErrorWhenScannedDocumentsAreNull() {
        CaseDetails caseDetails = caseDetails();
        CaseData caseData = CaseData.builder().ccdCaseReference(CASE_ID_LONG).scannedDocuments(null).build();
        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document is not available for download");
    }

    @Test
    void shouldReturnServerErrorWhenScannedDocumentsAreEmpty() {
        CaseDetails caseDetails = caseDetails();
        CaseData caseData = CaseData.builder().ccdCaseReference(CASE_ID_LONG).scannedDocuments(List.of()).build();
        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document is not available for download");
    }

    @Test
    void shouldReturnServerErrorWhenMatchingDocumentIsMissing() {
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.OTHER)
            .subtype("different-subtype")
            .url(document())
            .build();
        CaseDetails caseDetails = caseDetails();
        CaseData caseData = caseDataWithDocuments(scannedDocument);

        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document is not available for download");
    }

    @Test
    void shouldReturnServerErrorWhenDocumentUrlIsMissing() {
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype("OCON9x")
            .build();
        CaseDetails caseDetails = caseDetails();
        CaseData caseData = caseDataWithDocuments(scannedDocument);

        when(coreCaseDataService.getCase(eq(CASE_ID_LONG), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        assertThatThrownBy(() -> mockMvc.perform(
            get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
        ))
            .hasCauseInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Document URL is missing");
    }

    private static CaseDetails caseDetails() {
        return CaseDetails.builder().id(CASE_ID_LONG).build();
    }

    private static CaseData caseDataWithDocuments(ScannedDocument scannedDocument) {
        return CaseData.builder()
            .ccdCaseReference(CASE_ID_LONG)
            .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
            .build();
    }

    private static ScannedDocument scannedDocumentWithSubtype(String subtype) {
        return ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype(subtype)
            .url(document())
            .build();
    }

    private static Document document() {
        return new Document()
            .setDocumentUrl("http://dm-store/documents/doc-123")
            .setDocumentBinaryUrl("http://dm-store/documents/doc-123/binary")
            .setDocumentFileName("ocon9x.pdf");
    }
}
