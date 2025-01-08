package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeOfDiscontinuanceLiPLetterGeneratorTest {

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @InjectMocks
    private NoticeOfDiscontinuanceLiPLetterGenerator liPLetterGenerator;
    private static final String NOTICE_OF_DISCONTINUANCE_LETTER = "notice-of-discontinuance";
    private static final String BEARER_TOKEN = "Bearer Token";

    static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};

    private static final CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName("document name")
            .documentType(DocumentType.NOTICE_OF_DISCONTINUANCE)
            .build();

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1(applicant)
                .respondent1(defendant)
                .respondent1NoticeOfDiscontinueAllPartyViewDoc(caseDocument
                ).build();

        given(documentDownloadService.downloadDocument(
                any(),
                any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);

        verify(bulkPrintService)
                .printLetter(
                        LETTER_CONTENT,
                        caseData.getLegacyCaseReference(),
                        caseData.getLegacyCaseReference(),
                        NOTICE_OF_DISCONTINUANCE_LETTER,
                        List.of(caseData.getRespondent1().getPartyName()));
    }

    @Test
    void shouldNotDownloadDocumentAndPrintLetterSuccessfully() {
        Party applicant = PartyBuilder.builder().soleTrader().build();
        Party defendant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1(applicant)
                .respondent1(defendant)
                .respondent1NoticeOfDiscontinueAllPartyViewDoc(null).build();

        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);

        verify(bulkPrintService, never())
                .printLetter(
                        LETTER_CONTENT,
                        caseData.getLegacyCaseReference(),
                        caseData.getLegacyCaseReference(),
                        NOTICE_OF_DISCONTINUANCE_LETTER,
                        List.of(caseData.getRespondent1().getPartyName()));
    }
}
