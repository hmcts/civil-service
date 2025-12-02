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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoCoverLetterAppendService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendSDOBulkPrintServiceTest {

    @InjectMocks
    private SendSDOBulkPrintService sendSDOBulkPrintService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private FeatureToggleService featureToggleService;

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
    void shouldPrintLetterSuccessfullyForDefendantLIPInEnglishIfWelshNotEnabled() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithSDOOrder(applicant1);
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForDefendantLIPInWelsh() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithTranslatedSDOOrder(applicant1);
        RespondentLiPResponse respondentLiPResponse2 = new RespondentLiPResponse();
        respondentLiPResponse2.setRespondent1ResponseLanguage("WELSH");
        CaseDataLiP caseDataLiP2 = new CaseDataLiP();
        caseDataLiP2.setRespondent1LiPResponse(respondentLiPResponse2);
        caseData.setCaseDataLiP(caseDataLiP2);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForDefendantLIPInBothLanguages() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithEnglishAndTranslatedSDOOrder(applicant1);
        RespondentLiPResponse respondentLiPResponse3 = new RespondentLiPResponse();
        respondentLiPResponse3.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP3 = new CaseDataLiP();
        caseDataLiP3.setRespondent1LiPResponse(respondentLiPResponse3);
        caseData.setCaseDataLiP(caseDataLiP3);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void shouldPrintLetterSuccessfullyForClaimantLIPInEnglishIfWelshNotEnabled() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithSDOOrder(applicant1);
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("WELSH").build();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForClaimantLIPInWelsh() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("WELSH").build();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForClaimantLIPInBothLanguages() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithEnglishAndTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
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
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SDO_ORDER);
        caseDocument.setDocumentLink(DOCUMENT_LINK);
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .respondent1(party)
            .applicant1(party)
            .build();
    }

    private CaseData createCaseDataWithTranslatedSDOOrder(Party party) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SDO_TRANSLATED_DOCUMENT);
        caseDocument.setDocumentLink(DOCUMENT_LINK);
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .respondent1(party)
            .applicant1(party)
            .build();
    }

    private CaseData createCaseDataWithEnglishAndTranslatedSDOOrder(Party party) {
        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentType(SDO_ORDER);
        caseDocument1.setDocumentLink(DOCUMENT_LINK);
        CaseDocument caseDocument2 = new CaseDocument();
        caseDocument2.setDocumentType(SDO_TRANSLATED_DOCUMENT);
        caseDocument2.setDocumentLink(DOCUMENT_LINK);
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument1, caseDocument2))
            .respondent1(party)
            .applicant1(party)
            .build();
    }

    private CaseData createCaseDataWithSealedClaim() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .build();
    }
}
