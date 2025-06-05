package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    private static final String TEST = "test";
    private static final String UPLOAD_TIMESTAMP = "04 Jun 2025 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);

    static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    @Mock
    private FeatureToggleService featureToggleService;

    private static final CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName("document name")
            .documentType(DocumentType.NOTICE_OF_DISCONTINUANCE)
            .build();

    private void verifyPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            NOTICE_OF_DISCONTINUANCE_LETTER,
            List.of(party.getPartyName())
        );
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("ENGLISH").build()).build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                          .documents(Language.ENGLISH).build()).build())
            .respondent1(respondent1)
            .respondent1NoticeOfDiscontinueAllPartyViewDoc(caseDocument).build();

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);

        // then
        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyWhenWelshParty() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("ENGLISH").build()).build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                          .documents(Language.WELSH).build()).build())
            .respondent1(respondent1)
            .respondent1NoticeOfDiscontinueAllPartyViewDoc(caseDocument)
            .respondent1NoticeOfDiscontinueAllPartyTranslatedDoc(caseDocument).build();

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        // when
        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);

        // then
        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyWhenMainCaseHasBilingualParty() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("ENGLISH").build()).build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                          .documents(Language.BOTH).build()).build())
            .respondent1(respondent1)
            .respondent1NoticeOfDiscontinueAllPartyViewDoc(caseDocument)
            .respondent1NoticeOfDiscontinueAllPartyTranslatedDoc(caseDocument).build();

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);

        // then
        verifyPrintLetter(caseData, respondent1);
    }


    @Test
    void shouldNotDownloadDocumentAndPrintLetterSuccessfully() {
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .respondent1(respondent1)
            .respondent1NoticeOfDiscontinueAllPartyViewDoc(null).build();

        liPLetterGenerator.printNoticeOfDiscontinuanceLetter(caseData, BEARER_TOKEN);
        // then
        verify(bulkPrintService, never()).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            NOTICE_OF_DISCONTINUANCE_LETTER,
            List.of(respondent1.getPartyName())
        );
    }
}
