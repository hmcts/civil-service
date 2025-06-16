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
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SendHearingBulkPrintServiceTest {

    @Mock
    private CoverLetterAppendService coverLetterAppendService;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private SendHearingBulkPrintService sendHearingBulkPrintService;
    @Mock
    private FeatureToggleService featureToggleService;

    private static final String SDO_HEARING_PACK_LETTER_TYPE = "hearing-document-pack";
    public static final String TASK_ID_DEFENDANT = "SendHearingToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendHearingToClaimantLIP";
    public static final String TASK_ID_DEFENDANT_HMC = "SendAutomaticHearingToDefendantLIP";
    private static final String TEST = "test";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private CaseData buildCaseData(Party party, DocumentType documentType, boolean addHearingDocuments,
                                   String respondentResponse, String claimIssueLang, Language appDocLang, Language defDocLang) {
        CaseDocument caseDocument = CaseDocument.builder().documentType(documentType).documentLink(DOCUMENT_LINK).build();
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .claimantBilingualLanguagePreference(claimIssueLang)
            .caseDataLip(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(respondentResponse).build()).build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQLanguage(WelshLanguageRequirements.builder()
                                                        .documents(appDocLang).build()).build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                          .documents(defDocLang).build()).build())
            .respondent1(party)
            .applicant1(party);

        if (addHearingDocuments) {
            return caseDataBuilder.build().toBuilder().hearingDocuments(wrapElements(caseDocument))
                .hearingDocumentsWelsh(wrapElements(caseDocument)).build();
        }

        return caseDataBuilder.build();
    }

    private void verifyPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            SDO_HEARING_PACK_LETTER_TYPE,
            List.of(party.getPartyName())
        );
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyHMC() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_HMC, false);

        // then
        verifyPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT, false);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenHearingOrderAbsent() {
        // given
        CaseData caseData = buildCaseData(null, SEALED_CLAIM, false, null, null, null, null);

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenHearingOrderDocumentIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(new CaseDocument[] {null})) // Adding a null CaseDocument explicitly
            .build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null)
            .respondent1(PartyBuilder.builder().individual().build())
            .build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, false);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT, true);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfullyWhenWelsh() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, "WELSH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT, true);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfullyWhenBilingual() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, "BOTH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT, true);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToDefendantLiPSuccessfullyWhenBilingual() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, "BOTH", null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, true);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToDefendantLiPSuccessfullyWhenEnglish() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, "ENGLISH", null, null, Language.WELSH);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, true);

        // then
        verifyPrintLetter(caseData, claimant);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseWelshDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, true);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseWelshDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT, true);

        // then
        verifyNoInteractions(bulkPrintService);
    }
}
