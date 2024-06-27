package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendSDOBulkPrintServiceTest {

    @InjectMocks
    private SendSDOBulkPrintService sendSDOBulkPrintService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private BulkPrintService bulkPrintService;

    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        Party respondent1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithSDOOrder(respondent1);

        givenDocumentDownloadWillReturn();

        sendSDOBulkPrintService.sendSDOToDefendantLIP(BEARER_TOKEN, caseData);

        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        sendSDOBulkPrintService.sendSDOToDefendantLIP(BEARER_TOKEN, caseData);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSDOOrderAbsent() {
        CaseData caseData = createCaseDataWithSealedClaim();

        sendSDOBulkPrintService.sendSDOToDefendantLIP(BEARER_TOKEN, caseData);

        verifyNoInteractions(bulkPrintService);
    }

    private void givenDocumentDownloadWillReturn() {
        given(documentDownloadService.downloadDocument(any(), any()))
            .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), TEST, TEST));
    }

    private void verifyPrintLetter(CaseData caseData, Party respondent1) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            SDO_ORDER_PACK_LETTER_TYPE,
            Collections.singletonList(respondent1.getPartyName())
        );
    }

    private Party createSoleTraderParty() {
        return PartyBuilder.builder().soleTrader().build();
    }

    private CaseData createCaseDataWithSDOOrder(Party respondent1) {
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SDO_ORDER)
                                                           .documentLink(DOCUMENT_LINK)
                                                           .build()))
            .respondent1(respondent1)
            .build();
    }

    private CaseData createCaseDataWithSealedClaim() {
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SEALED_CLAIM)
                                                           .build()))
            .build();
    }
}
