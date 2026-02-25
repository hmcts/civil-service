package uk.gov.hmcts.reform.civil.ga.service;

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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationParty;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.PostOrderCoverLetter;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
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

    private GeneralApplicationCaseData buildCaseData() {
        return new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .claimant1PartyName("claimant1")
                .defendant1PartyName("defendant1")
                .ccdCaseReference(12345L)
                .applicationIsCloaked(YesOrNo.NO)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("123"))
                .build();
    }

    private GeneralApplicationCaseData buildCivilCaseData() {
        return new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .legacyCaseReference("00MC2")
                .applicant1(new GeneralApplicationParty()
                        .setPrimaryAddress(buildAddress("postcode", "posttown", "address1", "address2", "address3"))
                        .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                        .setPartyName("applicant1partyname"))
                .respondent1(new GeneralApplicationParty()
                        .setPrimaryAddress(buildAddress(
                            "respondent1postcode",
                            "respondent1posttown",
                            "respondent1address1",
                            "respondent1address2",
                            "respondent1address3"
                        ))
                        .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                        .setPartyName("respondent1partyname"))
                .build();
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
                .setDocumentBinaryUrl("binaryUrl");

        GeneralApplicationParty applicant = new GeneralApplicationParty()
                .setPrimaryAddress(buildAddress("postcode", "posttown", "address1", "address2", "address3"))
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("applicant1partyname");

        GeneralApplicationCaseData caseData = buildCaseData();
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderPrintService.sendJudgeFinalOrderToPrintForLIP(BEARER_TOKEN, document, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

        // then
        verifyPrintLetter(civilCaseData, caseData, applicant, List.of(document.getDocumentFileName()));
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyRespondent() {
        // given
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
                .setDocumentBinaryUrl("binaryUrl");

        GeneralApplicationParty respondent = new GeneralApplicationParty()
                .setPrimaryAddress(buildAddress(
                    "respondent1postcode",
                    "respondent1posttown",
                    "respondent1address1",
                    "respondent1address2",
                    "respondent1address3"
                ))
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("respondent1partyname");

        GeneralApplicationCaseData caseData = buildCaseData();
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));

        // when
        sendFinalOrderPrintService.sendJudgeFinalOrderToPrintForLIP(BEARER_TOKEN, document, caseData, civilCaseData, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

        // then
        verifyPrintLetter(civilCaseData, caseData, respondent, List.of(document.getDocumentFileName()));
    }

    private static Address buildAddress(String postCode, String postTown, String addressLine1, String addressLine2, String addressLine3) {
        Address address = new Address();
        address.setPostCode(postCode);
        address.setPostTown(postTown);
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        return address;
    }

    @Test
    void shouldNotStitchAndPrintTranslatedLetterWhenStitchingNotEnabled() {
        // given
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toGeneralApplicationCaseData(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                new DocmosisDocument());
        given(documentManagementService.uploadDocument(any(), (PDF) any())).willReturn(new CaseDocument());
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
                .setDocumentBinaryUrl("binaryUrl");
        GeneralApplicationCaseData caseData = buildCaseData();
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyRespondentWhenClaimantIsApplicant() {
        // given
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toGeneralApplicationCaseData(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                new DocmosisDocument());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(new CaseDocument());
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
            .setDocumentBinaryUrl("binaryUrl");
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(new CaseDocument()
                .setDocumentLink(document));
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        GeneralApplicationCaseData caseData = buildCaseData();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        GeneralApplicationParty respondent = new GeneralApplicationParty()
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("respondent1partyname");
        verifyPrintTranslatedLetter(civilCaseData, caseData, respondent, List.of(document.getDocumentFileName()));
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyApplicantWhenClaimantIsApplicant() {
        // given
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toGeneralApplicationCaseData(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                new DocmosisDocument());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(new CaseDocument());
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
            .setDocumentBinaryUrl("binaryUrl");
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(new CaseDocument()
                .setDocumentLink(document));
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        GeneralApplicationCaseData caseData = buildCaseData();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);

        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT);

        // then
        GeneralApplicationParty applicant = new GeneralApplicationParty()
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("applicant1partyname");
        verifyPrintTranslatedLetter(civilCaseData, caseData, applicant, List.of(document.getDocumentFileName()));
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyRespondentWhenClaimantIsRespondent() {
        // given
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toGeneralApplicationCaseData(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                new DocmosisDocument());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(new CaseDocument());
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
            .setDocumentBinaryUrl("binaryUrl");
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(new CaseDocument()
                .setDocumentLink(document));
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        GeneralApplicationCaseData caseData = buildCaseData();
        caseData = caseData.copy().parentClaimantIsApplicant(YesOrNo.NO).build();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);
        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

        // then
        GeneralApplicationParty applicant = new GeneralApplicationParty()
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("applicant1partyname");
        verifyPrintTranslatedLetter(civilCaseData, caseData, applicant, List.of(document.getDocumentFileName()));
    }

    @Test
    void shouldStitchAndPrintTranslatedLetterSuccessfullyApplicantWhenClaimantIsRespondent() {
        // given
        GeneralApplicationCaseData civilCaseData = buildCivilCaseData();
        given(coreCaseDataService.getCase(any())).willReturn(CaseDetails.builder().build());
        given(caseDetailsConverter.toGeneralApplicationCaseData(any())).willReturn(civilCaseData);
        given(documentGeneratorService.generateDocmosisDocument(any(PostOrderCoverLetter.class), eq(POST_ORDER_COVER_LETTER_LIP))).willReturn(
                new DocmosisDocument());
        given(documentManagementService.uploadDocument((String) any(), (PDF) any())).willReturn(new CaseDocument());
        Document document = new Document().setDocumentUrl("url").setDocumentFileName("filename").setDocumentHash("hash")
            .setDocumentBinaryUrl("binaryUrl");
        given(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any())).willReturn(new CaseDocument()
                .setDocumentLink(document));
        given(documentDownloadService.downloadDocument(any(), any()))
                .willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), "test", "test"));
        GeneralApplicationCaseData caseData = buildCaseData();
        caseData = caseData.copy().parentClaimantIsApplicant(YesOrNo.NO).build();
        ReflectionTestUtils.setField(sendFinalOrderPrintService, "stitchEnabled", true);

        // when
        sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(BEARER_TOKEN, document, document, caseData, CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT);

        // then
        GeneralApplicationParty respondent = new GeneralApplicationParty()
                .setType(GeneralApplicationParty.Type.INDIVIDUAL)
                .setPartyName("respondent1partyname");
        verifyPrintTranslatedLetter(civilCaseData, caseData, respondent, List.of(document.getDocumentFileName()));
    }

    private void verifyPrintLetter(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData,
                                   GeneralApplicationParty party,
                                   List<String> bulkPrintFileNames) {
        verify(bulkPrintService).printLetter(
                LETTER_CONTENT,
                caseData.getGeneralAppParentCaseLink().getCaseReference(),
                civilCaseData.getLegacyCaseReference(),
                FINAL_ORDER_PACK_LETTER_TYPE,
                List.of(party.getPartyName()),
                bulkPrintFileNames
        );
    }

    private void verifyPrintTranslatedLetter(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData,
                                             GeneralApplicationParty party,
                                             List<String> bulkPrintFileNames) {
        verify(bulkPrintService).printLetter(
                LETTER_CONTENT,
                caseData.getGeneralAppParentCaseLink().getCaseReference(),
                civilCaseData.getLegacyCaseReference(),
                TRANSLATED_ORDER_PACK_LETTER_TYPE,
                List.of(party.getPartyName()),
                bulkPrintFileNames
        );
    }

}
