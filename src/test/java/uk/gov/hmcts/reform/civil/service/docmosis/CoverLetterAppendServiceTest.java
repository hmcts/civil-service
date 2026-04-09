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
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.COVER_LETTER;

@ExtendWith(MockitoExtension.class)
class CoverLetterAppendServiceTest {

    @InjectMocks
    private CoverLetterAppendService coverLetterAppendService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final Address APPLICANT_ADDRESS = address("123 road", "London", "SW1 1NT");
    private static final Address RESPONDENT_ADDRESS = address("456 Avenue", "London", "EX12RT");

    private static final Party CLAIMANT = new Party()
        .setPrimaryAddress(APPLICANT_ADDRESS)
        .setType(Party.Type.INDIVIDUAL)
        .setIndividualTitle("Mr.")
        .setIndividualFirstName("Mint")
        .setIndividualLastName("Clay");

    private static final Party DEFENDANT = new Party()
        .setPrimaryAddress(RESPONDENT_ADDRESS)
        .setType(Party.Type.INDIVIDUAL)
        .setIndividualTitle("Mr.")
        .setIndividualFirstName("Indent")
        .setIndividualLastName("Dave");

    private static Address address(String addressLine1, String postTown, String postCode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setPostTown(postTown);
        address.setPostCode(postCode);
        return address;
    }

    private static final CoverLetter CLAIMANT_LETTER_TEMPLATE_DATA = new CoverLetter()
        .setParty(CLAIMANT);
    private static final CoverLetter DEFENDANT_LETTER_TEMPLATE_DATA = new CoverLetter()
        .setParty(DEFENDANT);

    private static final CaseDocument caseDocument = new CaseDocument()
        .setDocumentType(DocumentType.HEARING_FORM)
        .setDocumentSize(5L)
        .setDocumentName("DocumentName.pdf")
        .setCreatedBy("CIVIL")
        .setCreatedDatetime(LocalDateTime.of(2024,  1, 2,  3,  4))
        .setDocumentLink(new Document().setDocumentFileName("DocumentName.pdf").setDocumentBinaryUrl("Binary/url").setDocumentUrl("url"));
    private static final byte[] STITCHED_DOC_BYTES = new byte[]{1, 2, 3, 4};

    private List<DocumentMetaData> specClaimTimelineDocuments;

    @BeforeEach
    void setup() {
        given(documentManagementService.uploadDocument(any(), any(PDF.class))).willReturn(caseDocument);
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(STITCHED_DOC_BYTES), "test", "test"));
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                             anyString())).thenReturn(buildStitchedDocument());

        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(new DocmosisDocument().setBytes(new byte[]{1, 2, 3, 4, 5, 6}));

        specClaimTimelineDocuments = List.of(
            new DocumentMetaData(caseDocument.getDocumentLink(), "Cover letter", LocalDate.now().toString()),
            new DocumentMetaData(caseDocument.getDocumentLink(), "Document to attach", LocalDate.now().toString())
        );
    }

    @Test
    void shouldGenerateMailableLetterSuccessfullyForClaimant() {
        // Given
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

        // When
        byte[] mailableLetter = coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, CLAIMANT, SEALED_CLAIM,
                                                                              caseDocument
        );

        // Then
        assertThat(mailableLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(CLAIMANT_LETTER_TEMPLATE_DATA, COVER_LETTER);
        verify(civilStitchService).generateStitchedCaseDocument(specClaimTimelineDocuments, "DocumentName.pdf", 1L,
                                                                SEALED_CLAIM, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateMailableLetterSuccessfullyForDefendant_forMultipleDocuments() {
        // Given
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

        // When
        byte[] downloadedLetter = coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, DEFENDANT, SEALED_CLAIM,
                                                                                caseDocument, caseDocument
        );

        // Then
        assertThat(downloadedLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(DEFENDANT_LETTER_TEMPLATE_DATA, COVER_LETTER);
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>(specClaimTimelineDocuments);
        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(), "Document to attach", LocalDate.now().toString()));
        verify(civilStitchService).generateStitchedCaseDocument(documentMetaDataList, "DocumentName.pdf", 1L,
                                                                SEALED_CLAIM, BEARER_TOKEN);
    }

    @Test
    void shouldThrowExceptionWhenDownloadIsEmpty() {
        // Given
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(null, null, null));
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

        // Then
        assertThrows(DocumentDownloadException.class, () ->
            coverLetterAppendService.makeDocumentMailable(caseData, BEARER_TOKEN, CLAIMANT, SEALED_CLAIM,
                                                          caseDocument));
    }

    private CaseDocument buildStitchedDocument() {
        return new CaseDocument()
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
}
