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
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
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
        caseData = caseData.toBuilder().caseDataLiP(
            CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
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
        caseData = caseData.toBuilder().caseDataLiP(
            CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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
        caseData = caseData.toBuilder().caseDataLiP(
            CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void shouldPrintLetterSuccessfullyForDefendantLIPInWelshIfDocPreferenceIsWelsh() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().documents(Language.WELSH).build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForRespondentLIPInBothLanguagesIfDocPreferenceIsBoth() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithEnglishAndTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().documents(Language.BOTH).build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
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
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void shouldPrintLetterSuccessfullyForClaimantLIPInWelshIfDocPreferenceIsWelsh() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().applicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().documents(Language.WELSH).build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(sdoCoverLetterAppendService.makeSdoDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        sendSDOBulkPrintService.sendSDOOrderToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        verifyPrintLetter(caseData, applicant1);
        ArgumentCaptor<CaseDocument[]> captor = ArgumentCaptor.forClass(CaseDocument[].class);
        verify(sdoCoverLetterAppendService).makeSdoDocumentMailable(any(), any(), any(), any(), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldPrintLetterSuccessfullyForClaimantLIPInBothLanguagesIfDocPreferenceIsBoth() {
        Party applicant1 = createSoleTraderParty();
        CaseData caseData = createCaseDataWithEnglishAndTranslatedSDOOrder(applicant1);
        caseData = caseData.toBuilder().applicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().documents(Language.BOTH).build()).build()).build();
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SDO_ORDER)
                                                           .documentLink(DOCUMENT_LINK)
                                                           .build()))
            .respondent1(party)
            .applicant1(party)
            .build();
    }

    private CaseData createCaseDataWithTranslatedSDOOrder(Party party) {
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SDO_TRANSLATED_DOCUMENT)
                                                           .documentLink(DOCUMENT_LINK)
                                                           .build()))
            .respondent1(party)
            .applicant1(party)
            .build();
    }

    private CaseData createCaseDataWithEnglishAndTranslatedSDOOrder(Party party) {
        return CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                           .documentType(SDO_ORDER)
                                                           .documentLink(DOCUMENT_LINK)
                                                           .build(),
                                                       CaseDocument.builder()
                                                           .documentType(SDO_TRANSLATED_DOCUMENT)
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
