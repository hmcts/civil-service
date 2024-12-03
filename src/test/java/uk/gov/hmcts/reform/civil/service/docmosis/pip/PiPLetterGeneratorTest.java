package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.PIP_LETTER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@ExtendWith(MockitoExtension.class)
class PiPLetterGeneratorTest {

    @InjectMocks
    private PiPLetterGenerator piPLetterGenerator;

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final LocalDateTime RESPONSE_DEADLINE = LocalDateTime.now();
    private static final Address RESPONDENT_ADDRESS = Address.builder()
        .addressLine1("123 road")
        .postTown("London")
        .postCode("EX12RT")
        .build();
    private static final String CLAIMANT_FULL_NAME = "Mr. John Smith";
    private static final String CLAIM_REFERENCE = "ABC";
    private static final Party DEFENDANT = Party.builder()
        .primaryAddress(RESPONDENT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Smith")
        .individualLastName("John")
        .build();
    private static final BigDecimal TOTAL_CLAIM_AMOUNT = new BigDecimal("1000");
    private static final String PIN = "1234789";
    private static final String CUI_URL = "CUI response url";
    private static final PiPLetter LETTER_TEMPLATE_DATA = PiPLetter.builder()
        .pin(PIN)
        .ccdCaseNumber("1234123412341234")
        .claimantName(CLAIMANT_FULL_NAME)
        .claimReferenceNumber(CLAIM_REFERENCE)
        .issueDate(LocalDate.now())
        .defendant(DEFENDANT)
        .responseDeadline(RESPONSE_DEADLINE.toLocalDate())
        .totalAmountOfClaim(TOTAL_CLAIM_AMOUNT)
        .respondToClaimUrl(CUI_URL)
        .build();
    private static final byte[] STITCHED_DOC_BYTES = new byte[]{1, 2, 3, 4};

    private List<DocumentMetaData> specClaimTimelineDocuments;

    @BeforeEach
    void setup() {
        given(documentManagementService.uploadDocument(any(), any(PDF.class))).willReturn(buildClaimFormDocument());
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(STITCHED_DOC_BYTES), "test", "test"));
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(PIP_LETTER),
                                                             anyString())).thenReturn(buildStitchedDocument());
        given(pipInPostConfiguration.getRespondToClaimUrl()).willReturn(CUI_URL);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(DocmosisDocument.builder().bytes(new byte[]{1, 2, 3, 4, 5, 6}).build());

        specClaimTimelineDocuments = List.of(
            new DocumentMetaData(buildClaimFormDocument().getDocumentLink(), "PiP Letter", LocalDate.now().toString()),
            new DocumentMetaData(buildClaimFormDocument().getDocumentLink(), "Sealed Claim form", LocalDate.now().toString())
        );
    }

    @Test
    void shouldGenerateAndDownloadLetterSuccessfully() {
        // Given
        CaseData caseData = buildCaseData(YesOrNo.NO, null);

        // When
        byte[] downloadedLetter = piPLetterGenerator.downloadLetter(caseData, BEARER_TOKEN);

        // Then
        assertThat(downloadedLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(LETTER_TEMPLATE_DATA, PIN_IN_THE_POST_LETTER);
    }

    @Test
    void shouldGenerateClaimFormWithClaimTimeLineDocs_whenUploadedByApplicant() {
        // Given
        CaseData caseData = buildCaseData(YES, setupParticularsOfClaimDocs());

        // When
        piPLetterGenerator.downloadLetter(caseData, BEARER_TOKEN);

        // Then
        verify(documentGeneratorService).generateDocmosisDocument(LETTER_TEMPLATE_DATA, PIN_IN_THE_POST_LETTER);
        verify(civilStitchService).generateStitchedCaseDocument(eq(specClaimTimelineDocuments),
                                                                anyString(), anyLong(), eq(PIP_LETTER), anyString());
    }

    private CaseData buildCaseData(YesOrNo respondent1Represented, ServedDocumentFiles servedDocumentFiles) {
        return CaseData.builder()
            .legacyCaseReference(CLAIM_REFERENCE)
            .ccdCaseReference(1234123412341234L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("John")
                            .individualLastName("Smith").build())
            .respondent1(DEFENDANT)
            .respondent1Represented(YesOrNo.NO)
            .respondent1ResponseDeadline(RESPONSE_DEADLINE)
            .totalClaimAmount(TOTAL_CLAIM_AMOUNT)
            .systemGeneratedCaseDocuments(setupSystemGeneratedCaseDocs())
            .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
            .specRespondent1Represented(respondent1Represented)
            .servedDocumentFiles(servedDocumentFiles)
            .build();
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        Document documentLink = Document.builder()
            .documentUrl("url")
            .documentFileName("testFileName.pdf")
            .documentBinaryUrl("binary-url")
            .build();
        CaseDocument caseDocumentClaim = CaseDocument.builder()
            .documentType(SEALED_CLAIM)
            .documentLink(documentLink)
            .build();
        return List.of(ElementUtils.element(caseDocumentClaim));
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        Document document1 = Document.builder()
            .documentUrl("fake-url")
            .documentFileName("file-name")
            .documentBinaryUrl("binary-url")
            .build();
        Document document2 = Document.builder()
            .documentUrl("fake-url")
            .documentFileName("file-name")
            .documentBinaryUrl("binary-url")
            .build();
        return ServedDocumentFiles.builder()
            .timelineEventUpload(List.of(ElementUtils.element(document2)))
            .particularsOfClaimDocument(List.of(ElementUtils.element(document1)))
            .build();
    }

    private CaseDocument buildClaimFormDocument() {
        return CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("url")
                              .documentFileName("testFileName.pdf")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
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
