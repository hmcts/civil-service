package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    PiPLetterGenerator.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class,
})
class PiPLetterGeneratorTest {

    @MockBean
    private PinInPostConfiguration pipInPostConfiguration;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DocumentDownloadService documentDownloadService;
    @Autowired
    private PiPLetterGenerator piPLetterGenerator;
    @MockBean
    private FeatureToggleService featureToggleService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final LocalDateTime RESPONSE_DEADLINE = LocalDateTime.now();
    private static final Address RESPONDENT_ADDRESS = Address.builder().addressLine1("123 road")
        .postTown("London")
        .postCode("EX12RT")
        .build();
    private static final String CLAIMANT_FULL_NAME = "Mr. John Smith";
    private static final String CLAIM_REFERENCE = "ABC";
    private static final Party DEFENDANT = Party.builder().primaryAddress(RESPONDENT_ADDRESS)
        .type(Party.Type.INDIVIDUAL)
        .individualTitle("Mr.")
        .individualFirstName("Smith")
        .individualLastName("John")
        .build();
    private static final BigDecimal TOTAL_CLAIM_AMOUNT = new BigDecimal("1000");
    private static final String PIN = "1234789";
    private static final String testUrl = "url";
    private static final String testFileName = "testFileName.pdf";
    private static final String testBinaryUrl = "binary-url";
    private static final String CUI_URL = "CUI response url";
    private static final PiPLetter LETTER_TEMPLATE_DATA = PiPLetter.builder()
        .pin(PIN)
        .claimantName(CLAIMANT_FULL_NAME)
        .claimReferenceNumber(CLAIM_REFERENCE)
        .issueDate(LocalDate.now())
        .defendant(DEFENDANT)
        .responseDeadline(RESPONSE_DEADLINE.toLocalDate())
        .totalAmountOfClaim(TOTAL_CLAIM_AMOUNT)
        .respondToClaimUrl(CUI_URL)
        .build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .legacyCaseReference(CLAIM_REFERENCE)
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
        .build();

    private static final List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                testUrl).documentFileName(testFileName).documentBinaryUrl(testBinaryUrl).build()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        return systemGeneratedCaseDocuments;
    }

    private static final DocmosisDocument LETTER = DocmosisDocument.builder()
        .bytes(new byte[]{1, 2, 3, 4, 5, 6})
        .build();
    private static final CaseDocument CLAIM_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl(testUrl)
                              .documentFileName(testFileName)
                              .documentBinaryUrl(testBinaryUrl)
                              .build())
            .build();
    private static final byte[] STITCHED_DOC_BYTES = new byte[]{1, 2, 3, 4};

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

    List<DocumentMetaData> specClaimTimelineDocuments = new ArrayList<>();

    @BeforeEach
    void setup() {
        when(documentManagementService
                 .uploadDocument(any(), (PDF) any()))
            .thenReturn(CLAIM_FORM);
        when(documentDownloadService
                 .downloadDocument(any(), any()))
            .thenReturn(new DownloadedDocumentResponse(new ByteArrayResource(STITCHED_DOC_BYTES), "test", "test"));
        when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(),
                                                  any(CaseData.class))).thenReturn(STITCHED_DOC);
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "PiP Letter",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
    }

    @Test
    void shouldGenerateAndDownloadLetterSuccessfully() {
        //Given
        given(pipInPostConfiguration.getRespondToClaimUrl()).willReturn(CUI_URL);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(LETTER);
        //Whem
        byte[] downloadedLetter = piPLetterGenerator.downloadLetter(CASE_DATA, BEARER_TOKEN);
        //Then
        assertThat(downloadedLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService, times(1)).generateDocmosisDocument(
            refEq(LETTER_TEMPLATE_DATA),
            refEq(PIN_IN_THE_POST_LETTER)
        );
    }

    @Test
    void shouldGenerateClaimFormWithClaimTimeLineDocs_whenUploadedByApplicant() {
        //Given
        given(pipInPostConfiguration.getRespondToClaimUrl()).willReturn(CUI_URL);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(LETTER);
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(CLAIM_REFERENCE)
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
            .specRespondent1Represented(YES)
            .servedDocumentFiles(setupParticularsOfClaimDocs())
            .build();
        //When
        piPLetterGenerator.downloadLetter(caseData, BEARER_TOKEN);
        //Then
        verify(documentGeneratorService, times(1)).generateDocmosisDocument(
            refEq(LETTER_TEMPLATE_DATA),
            refEq(PIN_IN_THE_POST_LETTER)
        );
        verify(civilDocumentStitchingService).bundle(eq(specClaimTimelineDocuments), eq("BEARER_TOKEN"), eq("sealed_claim_form_000DC001.pdf"),
                                                     eq("sealed_claim_form_000DC001.pdf"), eq(caseData));
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        final String TEST_URL = "fake-url";
        final String TEST_FILE_NAME = "file-name";
        final String BIN_URL = "binary-url";

        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document1 = Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).documentBinaryUrl(BIN_URL).build();
        particularsOfClaim.add(ElementUtils.element(document1));
        Document document2 = Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).documentBinaryUrl(BIN_URL).build();
        List<Element<Document>> timelineOfEvents = new ArrayList<>();
        timelineOfEvents.add(ElementUtils.element(document2));
        return ServedDocumentFiles.builder().timelineEventUpload(timelineOfEvents)
            .particularsOfClaimDocument(particularsOfClaim).build();
    }

}
