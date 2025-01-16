package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private CaseData buildCaseData(Party party, uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType documentType, boolean addFinalOrder) {
        CaseDocument caseDocument = CaseDocument.builder().documentType(documentType).documentLink(DOCUMENT_LINK).build();
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .respondent1(party)
            .applicant1(party);

        if (addFinalOrder) {
            return caseDataBuilder.build().toBuilder().finalOrderDocumentCollection(wrapElements(caseDocument)).build();
        }

        return caseDataBuilder.build();
    }

    private CaseData buildCaseDataForTranslatedOrder(Party party) {
        CaseDocument translatedDocument = CaseDocument.builder()
            .documentType(DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT).documentLink(DOCUMENT_LINK).build();
        CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().caseDataLip(
                CaseDataLiP.builder()
                    .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build())
                    .build())
            .respondent1(party)
                .systemGeneratedCaseDocuments(wrapElements(translatedDocument))
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
            .applicant1(party);
        return caseDataBuilder.build();
    }

    private void verifyPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            FINAL_ORDER_PACK_LETTER_TYPE,
            List.of(party.getPartyName())
        );
    }

    private void verifyPrintTranslatedLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            TRANSLATED_ORDER_PACK_LETTER_TYPE,
            List.of(party.getPartyName())
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
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);

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
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintTranslatedLetter(caseData, defendant);
    }

    @Test
    void shouldNotDownloadDocumentAndNotPrintTranslatedLetterToClaimantLiPWhenCaseProgressionIsFalse() {
        // given
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay())
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build();
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(false);

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocumentAndNotPrintTranslatedLetterToClaimantLiPWhenThereIsNoTranslatedOrder() {
        // given
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .respondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay())
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.BOTH.toString())
            .build();
        given(featureToggleService.isCaseProgressionEnabled()).willReturn(true);

        // when
        sendFinalOrderBulkPrintService.sendTranslatedFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }
}
