package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    PiPLetterGenerator.class,
    JacksonAutoConfiguration.class,
})
class PiPLetterGeneratorTest {

    @MockBean
    private PinInPostConfiguration pipInPostConfiguration;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    @MockBean
    private DocumentManagementService documentManagementService;
    @Autowired
    private PiPLetterGenerator piPLetterGenerator;

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
        .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
        .build();
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

    List<DocumentMetaData> specClaimTimelineDocuments = new ArrayList<>();

    @BeforeEach
    void setup() {
        when(documentManagementService
                 .uploadDocument((String) any(), (PDF) any()))
            .thenReturn(CLAIM_FORM);

        when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
        when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(),
                                                  any(CaseData.class))).thenReturn(STITCHED_DOC);
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "PiP Letter",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Litigant in person claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
    }

    @Test
    void shouldGenerateAndDownloadLetterSuccessfully() {
        given(pipInPostConfiguration.getRespondToClaimUrl()).willReturn(CUI_URL);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .willReturn(LETTER);

        CaseDocument downloadedLetter = piPLetterGenerator.downloadLetter(CASE_DATA, BEARER_TOKEN);

        assertThat(downloadedLetter).isEqualTo(STITCHED_DOC);
        verify(documentGeneratorService, times(1)).generateDocmosisDocument(
            refEq(LETTER_TEMPLATE_DATA),
            refEq(PIN_IN_THE_POST_LETTER)
        );
    }

    @Test
    void shouldGenerateClaimFormWithClaimTimeLineDocs_whenUploadedByRespondent() {
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
            .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
            .specRespondent1Represented(YES)
            .specClaimTemplateDocumentFiles(new Document("fake-url",
                                                         "binary-url",
                                                         "file-name",
                                                         null, null))

            .specClaimDetailsDocumentFiles(new Document("fake-url",
                                                        "binary-url",
                                                        "file-name",
                                                        null, null))
            .build();


        CaseDocument downloadedLetter = piPLetterGenerator.downloadLetter(caseData, BEARER_TOKEN);

        verify(documentGeneratorService, times(1)).generateDocmosisDocument(
            refEq(LETTER_TEMPLATE_DATA),
            refEq(PIN_IN_THE_POST_LETTER)
        );
        verify(civilDocumentStitchingService).bundle(eq(specClaimTimelineDocuments), anyString(), anyString(),
                                                     anyString(), eq(caseData));
    }

}
