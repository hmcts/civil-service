package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.querymanagement.DocumentQueryMessage;
import uk.gov.hmcts.reform.civil.model.docmosis.querymanagement.QueryDocument;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.QUERY_DOCUMENT;

@ExtendWith(MockitoExtension.class)
class QueryDocumentGeneratorTest {

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private QueryDocumentGenerator queryDocumentGenerator;

    private static final Long CASE_ID = 12345L;
    private static final String AUTHORIZATION = "Bearer token";
    private static final DocCategory DOCUMENT_CATEGORY = DocCategory.CLAIMANT_QUERY_DOCUMENTS;
    private static final OffsetDateTime CREATED_ON = OffsetDateTime.now();
    private static final byte[] DOCUMENT_BYTES = "sample bytes".getBytes();
    private static final String SUBJECT = "Query Subject";
    private static final String CREATED_BY = "testUser";

    private List<Element<CaseMessage>> messageThread;
    private CaseDocument uploadedCaseDocument;

    @BeforeEach
    void setUp() {
        CaseMessage caseMessage = CaseMessage.builder()
            .subject(SUBJECT)
            .createdOn(CREATED_ON)
            .createdBy(CREATED_BY)
            .build();

        messageThread = List.of(Element.<CaseMessage>builder().value(caseMessage).build());

        QueryDocument queryDocument = QueryDocument.builder()
            .referenceNumber(CASE_ID.toString())
            .messages(List.of(DocumentQueryMessage.builder()
                                  .subject(SUBJECT)
                                  .createdOn(CREATED_ON.toString())
                                  .build()))
            .build();

        DocmosisDocument generatedDocmosisDocument = DocmosisDocument.builder()
            .documentTitle(String.format(QUERY_DOCUMENT.getDocumentTitle(), SUBJECT))
            .bytes(DOCUMENT_BYTES)
            .build();

        uploadedCaseDocument = CaseDocument.builder()
            .documentName(generatedDocmosisDocument.getDocumentTitle())
            .documentType(DocumentType.QUERY_DOCUMENT)
            .createdDatetime(CREATED_ON.toLocalDateTime())
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(QueryDocument.class), eq(QUERY_DOCUMENT)))
            .thenReturn(generatedDocmosisDocument);

        when(documentManagementService.uploadDocument(eq(AUTHORIZATION), any(PDF.class)))
            .thenReturn(uploadedCaseDocument);
    }

    @Test
    void shouldGenerateAndUploadQueryDocumentSuccessfully() {
        CaseDocument result = queryDocumentGenerator.generate(CASE_ID, messageThread, AUTHORIZATION, DOCUMENT_CATEGORY);

        assertThat(result).isEqualTo(uploadedCaseDocument);

        verify(documentGeneratorService).generateDocmosisDocument(any(QueryDocument.class), eq(QUERY_DOCUMENT));
        verify(documentManagementService).uploadDocument(eq(AUTHORIZATION), any(PDF.class));
        verify(assignCategoryId).assignCategoryIdToCaseDocument(uploadedCaseDocument, DOCUMENT_CATEGORY.getValue());
    }
}
