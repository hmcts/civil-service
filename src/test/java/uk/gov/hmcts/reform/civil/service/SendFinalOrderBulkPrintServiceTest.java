package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendFinalOrderBulkPrintServiceTest {

    @Mock
    private CoverLetterAppendService coverLetterAppendService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SendFinalOrderBulkPrintService sendFinalOrderBulkPrintService;

    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    private static final String TRANSLATED_ORDER_PACK_LETTER_TYPE = "translated-order-document-pack";
    public static final String TASK_ID_DEFENDANT = "SendFinalOrderToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendFinalOrderToClaimantLIP";
    private static final String TEST = "test";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private CaseData buildCaseData(Party party, DocumentType documentType, boolean addFinalOrder) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(documentType);
        caseDocument.setDocumentLink(DOCUMENT_LINK);
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .respondent1(party)
            .applicant1(party).build();

        if (addFinalOrder) {
            caseData.setFinalOrderDocumentCollection(wrapElements(caseDocument));
        }

        return caseData;
    }

    private CaseData buildCaseData(Party party) {
        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentType(JUDGE_FINAL_ORDER);
        caseDocument1.setDocumentLink(DOCUMENT_LINK);
        CaseDocument caseDocument2 = new CaseDocument();
        caseDocument2.setDocumentType(FINAL_ORDER_TRANSLATED_DOCUMENT);
        caseDocument2.setDocumentLink(DOCUMENT_LINK);

        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument1, caseDocument2))
            .respondent1(party)
            .applicant1(party).build();

        caseData.setFinalOrderDocumentCollection(wrapElements(caseDocument1, caseDocument2));
        return caseData;
    }

    private CaseData buildCaseDataForTranslatedOrder(Party party) {
        CaseDocument translatedDocument = new CaseDocument();
        translatedDocument.setDocumentType(DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT);
        translatedDocument.setDocumentLink(DOCUMENT_LINK);
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(party)
            .applicant1(party).build();
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setSystemGeneratedCaseDocuments(wrapElements(translatedDocument));
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        return caseData;
    }

    private void verifyPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            FINAL_ORDER_PACK_LETTER_TYPE,
            List.of(party.getPartyName()),
            List.of("test")
        );
    }

    private void verifyPrintTranslatedLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            TRANSLATED_ORDER_PACK_LETTER_TYPE,
            List.of(party.getPartyName()),
            List.of("test")
        );
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, JUDGE_FINAL_ORDER, true);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, JUDGE_FINAL_ORDER, true);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenFinalOrderOrderAbsent() {
        // given
        CaseData caseData = buildCaseData(null, SEALED_CLAIM, false);

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenFinalOrderOrderDocumentIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements((CaseDocument) null))
            .build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null)
            .respondent1(PartyBuilder.builder().individual().build()) // Adding a respondent to differentiate
            .build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDocumentAndPrintTranslatedLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().individual().build();
        CaseData caseData = buildCaseDataForTranslatedOrder(claimant);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintTranslatedLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDocumentAndPrintTranslatedLetterToDefendantLiPSuccessfully() {
        // given
        Party defendant = PartyBuilder.builder().individual().build();
        CaseData caseData = buildCaseDataForTranslatedOrder(defendant);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintTranslatedLetter(caseData, defendant);
    }

    @Test
    void shouldNotDownloadDocumentAndNotPrintTranslatedLetterToClaimantLiPWhenCaseProgressionIsFalse() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocumentAndNotPrintTranslatedLetterToClaimantLiPWhenThereIsNoTranslatedOrder() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPInWelshSuccessfully() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, FINAL_ORDER_TRANSLATED_DOCUMENT, true);
        caseData.setClaimantBilingualLanguagePreference("WELSH");
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant);
        ArgumentCaptor<CaseDocument[]> documentCaptor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(coverLetterAppendService).makeDocumentMailable(any(), any(), any(), any(DocumentType.class), documentCaptor.capture());
        assertThat(documentCaptor.getValue()).hasSize(1);
        assertThat(documentCaptor.getValue()[0].getDocumentType()).isEqualTo(FINAL_ORDER_TRANSLATED_DOCUMENT);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPInBothLanguagesSuccessfully() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant);
        caseData.setClaimantBilingualLanguagePreference("BOTH");
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetterWithDocumentNames(caseData, claimant);
        ArgumentCaptor<CaseDocument[]> documentCaptor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(coverLetterAppendService).makeDocumentMailable(any(), any(), any(), any(DocumentType.class), documentCaptor.capture());
        assertThat(documentCaptor.getValue()).hasSize(2);
        assertThat(documentCaptor.getValue()[0].getDocumentType()).isEqualTo(JUDGE_FINAL_ORDER);
        assertThat(documentCaptor.getValue()[1].getDocumentType()).isEqualTo(FINAL_ORDER_TRANSLATED_DOCUMENT);
    }

    private void verifyPrintLetterWithDocumentNames(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            FINAL_ORDER_PACK_LETTER_TYPE,
            List.of(party.getPartyName()),
            List.of("test", "test")
        );
    }
}
