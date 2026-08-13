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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScannedDocumentsControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CASE_ID = "1594901956117591";

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
        Document document = new Document()
            .setDocumentUrl("http://dm-store/documents/doc-123")
            .setDocumentBinaryUrl("http://dm-store/documents/doc-123/binary")
            .setDocumentFileName("ocon9x.pdf");

        ScannedDocument scannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype("OCON9x")
            .url(document)
            .build();

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.parseLong(CASE_ID))
            .scannedDocuments(List.of(new Element<>(UUID.randomUUID(), scannedDocument)))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(CASE_ID)).build();

        when(coreCaseDataService.getCase(eq(Long.parseLong(CASE_ID)), eq(AUTH_TOKEN))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(documentManagementService.downloadDocument(eq(AUTH_TOKEN), eq("http://dm-store/documents/doc-123")))
            .thenReturn(expectedPdf);

        mockMvc.perform(get("/scanned-documents/{externalId}/{documentType}/{documentSubtype}", CASE_ID, "FORM", "OCON9x")
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(content().bytes(expectedPdf));
    }
}
