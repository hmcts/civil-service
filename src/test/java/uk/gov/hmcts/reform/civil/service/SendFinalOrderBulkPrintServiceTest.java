package uk.gov.hmcts.reform.civil.service;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SendFinalOrderBulkPrintService.class,
    JacksonAutoConfiguration.class
})
class SendFinalOrderBulkPrintServiceTest {

    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @Autowired
    private SendFinalOrderBulkPrintService sendFinalOrderBulkPrintService;
    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    public static final String TASK_ID_DEFENDANT = "SendFinalOrderToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendFinalOrderToClaimantLIP";
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build()))
            .respondent1(respondent1)
            .build().toBuilder()
            .finalOrderDocumentCollection(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build())).build();
        given(documentDownloadService.downloadDocument(any(), any())).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);
        // then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                FINAL_ORDER_PACK_LETTER_TYPE,
                List.of(caseData.getRespondent1().getPartyName())
            );
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build()))
            .applicant1(claimant)
            .build().toBuilder()
            .finalOrderDocumentCollection(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build())).build();
        given(documentDownloadService.downloadDocument(any(), any())).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);
        // then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                FINAL_ORDER_PACK_LETTER_TYPE,
                List.of(caseData.getApplicant1().getPartyName())
            );
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
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build())).build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);
        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenFinalOrderOrderDocumentIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
            .build().toBuilder().finalOrderDocumentCollection(null).build();
        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsisNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(Lists.emptyList()).build();

        // when
        sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldReturnException_whenBulkPrintServiceReturnsIOException() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build()))
            .respondent1(respondent1)
            .build().toBuilder()
            .finalOrderDocumentCollection(wrapElements(CaseDocument.builder().documentType(JUDGE_FINAL_ORDER).documentLink(DOCUMENT_LINK).build())).build();
        given(documentDownloadService.downloadDocument(any(), any())).willReturn(new DownloadedDocumentResponse(null, null, null));

        // when // then
        assertThrows(DocumentDownloadException.class, () -> sendFinalOrderBulkPrintService.sendFinalOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT));

    }
}
