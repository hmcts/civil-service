package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.CoverLetter;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.COVER_LETTER;

@ExtendWith(MockitoExtension.class)
public class CoverLetterAppendServiceTest {

    @InjectMocks
    private CoverLetterAppendService coverLetterAppendService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private CivilDocumentStitchingService civilDocumentStitchingService;

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final LocalDateTime RESPONSE_DEADLINE = LocalDateTime.now();
    private static final Address APPLICANT_ADDRESS = Address.builder()
        .addressLine1("123 road")
        .postTown("London")
        .postCode("SW1 1NT")
        .build();
    private static final Address RESPONDENT_ADDRESS = Address.builder()
        .addressLine1("456 Avenue")
        .postTown("London")
        .postCode("EX12RT")
        .build();
    private static final String CLAIM_REFERENCE = "ABC";

    private static final Party CLAIMANT = Party.builder()
        .primaryAddress(APPLICANT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Mint")
        .individualLastName("Clay")
        .build();

    private static final Party DEFENDANT = Party.builder()
        .primaryAddress(RESPONDENT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Indent")
        .individualLastName("Dave")
        .build();
    private static final BigDecimal TOTAL_CLAIM_AMOUNT = new BigDecimal("1000");
    private static final String PIN = "1234789";
    private static final String CUI_URL = "CUI response url";
    private static final CoverLetter CLAIMANT_LETTER_TEMPLATE_DATA = CoverLetter.builder()
        .party(CLAIMANT)
        .build();
    private static final CoverLetter DEFENDANT_LETTER_TEMPLATE_DATA = CoverLetter.builder()
        .party(DEFENDANT)
        .build();

    private static final CaseDocument coverLetter = CaseDocument.builder()
        .documentType(DocumentType.COVER_LETTER)
        .documentSize(5L)
        .documentName("Cover letter.pdf")
        .createdBy("CIVIL")
        .createdDatetime(LocalDateTime.of(2024,  1, 2,  3,  4))
        .documentLink(Document.builder().documentFileName("Cover letter.pdf").documentBinaryUrl("Binary/url").documentUrl("url").build())
        .build();

    private static final CaseDocument caseDocument = CaseDocument.builder()
        .documentType(DocumentType.HEARING_FORM)
        .documentSize(5L)
        .documentName("DocumentName.pdf")
        .createdBy("CIVIL")
        .createdDatetime(LocalDateTime.of(2024,  1, 2,  3,  4))
        .documentLink(Document.builder().documentFileName("DocumentName.pdf").documentBinaryUrl("Binary/url").documentUrl("url").build())
        .build();
    private static final byte[] STITCHED_DOC_BYTES = new byte[]{1, 2, 3, 4};

    private List<DocumentMetaData> specClaimTimelineDocuments;

    @BeforeEach
    void setup() {
        given(documentManagementService.uploadDocument(any(), any(PDF.class))).willReturn(caseDocument);
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(STITCHED_DOC_BYTES), "test", "test"));
        given(civilDocumentStitchingService.bundle(anyList(), anyString(), anyString(), anyString(), any(CaseData.class)))
            .willReturn(buildStitchedDocument());
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(DocmosisDocument.builder().bytes(new byte[]{1, 2, 3, 4, 5, 6}).build());

        specClaimTimelineDocuments = List.of(
            new DocumentMetaData(caseDocument.getDocumentLink(), "Cover letter", LocalDate.now().toString()),
            new DocumentMetaData(caseDocument.getDocumentLink(), "Document to attach", LocalDate.now().toString())
        );
    }

    @Test
    void shouldGenerateMailableLetterSuccessfullyForClaimant() {
        // Given
        CaseData caseData = CaseData.builder().build();

        // When
        byte[] mailableLetter = coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, CLAIMANT,
                                                                                caseDocument
        );

        // Then
        assertThat(mailableLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(refEq(CLAIMANT_LETTER_TEMPLATE_DATA), refEq(COVER_LETTER));
        verify(civilDocumentStitchingService).bundle(specClaimTimelineDocuments, BEARER_TOKEN, "DocumentName.pdf",
                                                     "DocumentName.pdf", caseData);
    }

    @Test
    void shouldGenerateMailableLetterSuccessfullyForDefendant_forMultipleDocuments() {
        // Given
        CaseData caseData = CaseData.builder().build();

        // When
        byte[] downloadedLetter = coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, DEFENDANT,
                                                                                caseDocument, caseDocument
        );

        // Then
        assertThat(downloadedLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(refEq(DEFENDANT_LETTER_TEMPLATE_DATA), refEq(COVER_LETTER));
        List<DocumentMetaData> documentMetaDataList = specClaimTimelineDocuments.stream().collect(Collectors.toList());
        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(), "Document to attach", LocalDate.now().toString()));
        verify(civilDocumentStitchingService).bundle(documentMetaDataList, BEARER_TOKEN, "DocumentName.pdf",
                                                     "DocumentName.pdf", caseData);
    }

    @Test
    void shouldThrowExceptionWhenDownloadIsEmpty() {
        // Given
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(null, null, null));
        CaseData caseData = CaseData.builder().build();

        // Then
        assertThrows(DocumentDownloadException.class, () ->
            coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, CLAIMANT,
                                                          caseDocument));
    }

    private CaseDocument buildStitchedDocument() {
        return CaseDocument.builder()
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
}
