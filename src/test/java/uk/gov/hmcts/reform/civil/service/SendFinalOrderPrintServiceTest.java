package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.PostOrderCoverLetter;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_ORDER_COVER_LETTER_LIP;

@ExtendWith(MockitoExtension.class)
class SendFinalOrderPrintServiceTest {

    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private SendFinalOrderPrintService sendFinalOrderPrintService;

    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    private static final String TRANSLATED_ORDER_PACK_LETTER_TYPE = "translated-order-document-pack";
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private CaseData buildCaseData() {
        return CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .claimant1PartyName("claimant1")
                .defendant1PartyName("defendant1")
                .ccdCaseReference(12345L)
                .applicationIsCloaked(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
                .build();
    }

    private CaseData buildCivilCaseData() {
        return CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .legacyCaseReference("00MC2")
                .applicant1(Party.builder()
                        .primaryAddress(Address.builder()
                                .postCode("postcode")
                                .postTown("posttown")
                                .addressLine1("address1")
                                .addressLine2("address2")
                                .addressLine3("address3").build())
                        .type(Party.Type.INDIVIDUAL)
                        .partyName("applicant1partyname").build())
                .respondent1(Party.builder()
                        .primaryAddress(Address.builder()
                                .postCode("respondent1postcode")
                                .postTown("respondent1posttown")
                                .addressLine1("respondent1address1")
                                .addressLine2("respondent1address2")
                                .addressLine3("respondent1address3").build())
                        .type(Party.Type.INDIVIDUAL)
                        .partyName("respondent1partyname").build())
                .build();
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();

        Party applicant = Party.builder()
                .primaryAddress(Address.builder()
                        .postCode("postcode")
                        .postTown("posttown")
                        .addressLine1("address1")
                        .addressLine2("address2")
                        .addressLine3("address3").build())
                .type(Party.Type.INDIVIDUAL)
                .partyName("applicant1partyname").build();

        CaseData caseData = buildCaseData();
        CaseData civilCaseData = buildCivilCaseData();
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderPrintService.sendJudgeFinalOrderToPrintForLIP(BEARER_TOKEN, document, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

        // then
        verifyPrintLetter(civilCaseData, caseData, applicant);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyRespondent() {
        // given
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();

        Party respondent = Party.builder()
                .primaryAddress(Address.builder()
                        .postCode("respondent1postcode")
                        .postTown("respondent1posttown")
                        .addressLine1("respondent1address1")
                        .addressLine2("respondent1address2")
                        .addressLine3("respondent1address3").build())
                .type(Party.Type.INDIVIDUAL)
                .partyName("respondent1partyname").build();

        CaseData caseData = buildCaseData();
        CaseData civilCaseData = buildCivilCaseData();
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderPrintService.sendJudgeFinalOrderToPrintForLIP(BEARER_TOKEN, document, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

        // then
        verifyPrintLetter(civilCaseData, caseData, respondent);
    }

    @Test
    void shouldNotStitchAndPrintTranslatedLetterWhenStitchingNotEnabled() {
        // given
        CaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseDataGA(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                DocmosisDocument.builder().build());
        given(documentManagementService.uploadDocument(any(), (PDF) any())).willReturn(CaseDocument.builder().build());
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();
        CaseData caseData = buildCaseData();
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyRespondentWhenClaimantIsApplicant() {
        // given
        CaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseDataGA(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                DocmosisDocument.builder().build());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(CaseDocument.builder().build());
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(CaseDocument.builder()
                .documentLink(Document.builder()
                        .documentUrl("/test").build()).build());
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();
        CaseData caseData = buildCaseData();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        Party respondent = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("respondent1partyname").build();
        verifyPrintTranslatedLetter(civilCaseData, caseData, respondent);
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyApplicantWhenClaimantIsApplicant() {
        // given
        CaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseDataGA(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                DocmosisDocument.builder().build());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(CaseDocument.builder().build());
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(CaseDocument.builder()
                .documentLink(Document.builder()
                        .documentUrl("/test").build()).build());
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();
        CaseData caseData = buildCaseData();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);

        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT);

        // then
        Party applicant = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("applicant1partyname").build();
        verifyPrintTranslatedLetter(civilCaseData, caseData, applicant);
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyRespondentWhenClaimantIsRespondent() {
        // given
        CaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseDataGA(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                DocmosisDocument.builder().build());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(CaseDocument.builder().build());
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(CaseDocument.builder()
                .documentLink(Document.builder()
                        .documentUrl("/test").build()).build());
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();
        CaseData caseData = buildCaseData();
        caseData = caseData.toBuilder().parentClaimantIsApplicant(YesOrNo.NO).build();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        Party applicant = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("applicant1partyname").build();
        verifyPrintTranslatedLetter(civilCaseData, caseData, applicant);
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyApplicantWhenClaimantIsRespondent() {
        // given
        CaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toCaseDataGA(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                DocmosisDocument.builder().build());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(CaseDocument.builder().build());
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(CaseDocument.builder()
                .documentLink(Document.builder()
                        .documentUrl("/test").build()).build());
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        Document document = Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                .documentBinaryUrl("binaryUrl").build();
        CaseData caseData = buildCaseData();
        caseData = caseData.toBuilder().parentClaimantIsApplicant(YesOrNo.NO).build();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);

        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT);

        // then
        Party respondent = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .partyName("respondent1partyname").build();
        verifyPrintTranslatedLetter(civilCaseData, caseData, respondent);
    }

    private void verifyPrintLetter(CaseData civilCaseData, CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
                LETTER_CONTENT,
                caseData.getCcdCaseReference().toString(),
                civilCaseData.getLegacyCaseReference(),
                FINAL_ORDER_PACK_LETTER_TYPE,
                List.of(party.getPartyName())
        );
    }

    private void verifyPrintTranslatedLetter(CaseData civilCaseData, CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
                LETTER_CONTENT,
                caseData.getCcdCaseReference().toString(),
                civilCaseData.getLegacyCaseReference(),
                TRANSLATED_ORDER_PACK_LETTER_TYPE,
                List.of(party.getPartyName())
        );
    }

}
