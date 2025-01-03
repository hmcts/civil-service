package uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL;

@SpringBootTest(classes = {
    SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator.class,
    JacksonAutoConfiguration.class
})
class SettleClaimMarkedPaidInFullDefendantLiPLetterGeneratorTest {

    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @Autowired
    private SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator settleClaimMarkedPaidInFullDefendantLiPLetterGenerator;
    private static final String SETTLE_CLAIM_PAID_IN_FULL_LETTER = "settle-claim-paid-in-full-letter";
    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, TEST);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final CaseDocument SETTLE_CLAIM = CaseDocumentBuilder.builder()
        .documentName(null)
        .documentType(DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER)
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
                             DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER
                     )
                 ))
            .thenReturn(SETTLE_CLAIM);

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
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
                SETTLE_CLAIM_PAID_IN_FULL_LETTER,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

    @Test
    void shouldDownloadDocumentAndPrintBilingualLetterSuccessfully() {
        //Given
        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL)
        ))
            .thenReturn(new DocmosisDocument(
                SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL.getDocumentTitle(),
                LETTER_CONTENT
            ));
        when(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL.getDocumentTitle(),
                             LETTER_CONTENT,
                             DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER
                     )
                 ))
            .thenReturn(SETTLE_CLAIM);

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
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
                SETTLE_CLAIM_PAID_IN_FULL_LETTER,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

}
