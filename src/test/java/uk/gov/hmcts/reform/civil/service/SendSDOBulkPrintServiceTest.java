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
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoCoverLetterAppendService;
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
    private BulkPrintService bulkPrintService;


    @Mock
    private SdoCoverLetterAppendService sdoCoverLetterAppendService;

    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";
    private static final String TEST = "test";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final String TASK_ID_DEFENDANT = "SendSDOToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendSDOToClaimantLIP";

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForDefendantLIP() {
        Party respondent1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithSDOOrder(respondent1);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForClaimantLIP() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithSDOOrder(applicant1);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSDOOrderAbsent() {
        CaseData caseData = createCaseDataWithSealedClaim();

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyNoInteractions(bulkPrintService);
    }

    private void verifyPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            SDO_ORDER_PACK_LETTER_TYPE,
            Collections.singletonList(party.getPartyName())
        );
    }

    private Party createSoleTraderParty() {
        return PartyBuilder.builder().soleTrader().build();
    }

    private CaseData createCaseDataWithSDOOrder(Party party) {
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SDO_ORDER)
                                                           .documentLink(DOCUMENT_LINK)
                                                           .build()))
            .respondent1(party)
            .applicant1(party)
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
