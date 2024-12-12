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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    void shouldReturnStitchedDocuments() {
        byte[] docArray = {3, 5, 2, 4, 1};
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
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument LIP_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(LITIGANT_IN_PERSON_CLAIM_FORM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument STITCHED_DOC =
        CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

}
