package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH;

@ExtendWith(MockitoExtension.class)
class SettleClaimMarkedPaidInFullDefendantLiPLetterGeneratorTest {

    @InjectMocks
    private SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator settleClaimMarkedPaidInFullDefendantLiPLetterGenerator;

    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private CivilStitchService civilStitchService;

    private static final String SETTLE_CLAIM_PAID_IN_FULL_LETTER_STRING = "settle-claim-paid-in-full-letter";
    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, TEST);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private static final CaseDocument STITCHED_DOC =
        CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SETTLE_CLAIM_PAID_IN_FULL_LETTER)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument SETTLE_CLAIM = CaseDocumentBuilder.builder()
        .documentName(null)
        .documentType(SETTLE_CLAIM_PAID_IN_FULL_LETTER)
        .build();

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        //Given
        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER)
        ))
            .thenReturn(new DocmosisDocument(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                LETTER_CONTENT
            ));
        when(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                             LETTER_CONTENT,
                             SETTLE_CLAIM_PAID_IN_FULL_LETTER
                     )
                 ))
            .thenReturn(SETTLE_CLAIM);

        byte[] bytes = new ByteArrayResource(LETTER_CONTENT).getByteArray();
        given(documentDownloadService.downloadDocument(
            any(), any(), anyString(), anyString()
        )).willReturn(bytes);

        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .legacyCaseReference("100DC001")
            .build();

        //When
        settleClaimMarkedPaidInFullDefendantLiPLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(caseData, BEARER_TOKEN);

        //Then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                SETTLE_CLAIM_PAID_IN_FULL_LETTER_STRING,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

    @Test
    void shouldDownloadDocumentAndPrintBilingualLetterSuccessfully() {
        //Given
        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH)
        )).thenReturn(new DocmosisDocument(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH.getDocumentTitle(),
                LETTER_CONTENT
        ));

        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER)
        ))
            .thenReturn(new DocmosisDocument(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                LETTER_CONTENT
            ));

        when(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH.getDocumentTitle(),
                             LETTER_CONTENT,
                             SETTLE_CLAIM_PAID_IN_FULL_LETTER
                     )
                 ))
            .thenReturn(SETTLE_CLAIM);

        when(civilStitchService.generateStitchedCaseDocument(anyList(), any(), anyLong(), eq(SETTLE_CLAIM_PAID_IN_FULL_LETTER),
                                                             anyString())).thenReturn(STITCHED_DOC);

        byte[] bytes = new ByteArrayResource(LETTER_CONTENT).getByteArray();
        given(documentDownloadService.downloadDocument(
            any(), any(), anyString(), anyString()
        )).willReturn(bytes);

        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(123L)
            .respondent1Represented(YesOrNo.NO)
            .applicant1(applicant)
            .respondent1(defendant)
            .legacyCaseReference("100DC001")
            .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                                             .court(Language.WELSH)
                                                                             .build()).build())
            .build();

        //When
        settleClaimMarkedPaidInFullDefendantLiPLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(caseData, BEARER_TOKEN);

        //Then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                SETTLE_CLAIM_PAID_IN_FULL_LETTER_STRING,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }
}
