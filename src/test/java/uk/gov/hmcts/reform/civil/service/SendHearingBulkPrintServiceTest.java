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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
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
    private static final String DECISION_PACK_LETTER_TYPE = "decision-reconsider-document-pack";
    public static final String TASK_ID_DEFENDANT_DRO = "SendToDefendantLIP";
    public static final String TASK_ID_CLAIMANT_DRO = "SendDORToClaimantLIP";
    public static final String TASK_ID_DEFENDANT_HMC = "SendAutomaticHearingToDefendantLIP";
    private static final String TEST = "test";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private CaseData buildCaseData(Party party, DocumentType documentType, boolean addHearingDocuments,
                                   String respondentResponse, String claimIssueLang, Language appDocLang, Language defDocLang) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(documentType);
        caseDocument.setDocumentLink(DOCUMENT_LINK);
        
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage(respondentResponse);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        
        WelshLanguageRequirements appWelshLang = new WelshLanguageRequirements();
        appWelshLang.setDocuments(appDocLang);
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQLanguage(appWelshLang);
        
        WelshLanguageRequirements defWelshLang = new WelshLanguageRequirements();
        defWelshLang.setDocuments(defDocLang);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQLanguage(defWelshLang);
        
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(caseDocument))
            .respondent1(party)
            .applicant1(party).build();
        caseData.setClaimantBilingualLanguagePreference(claimIssueLang);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setApplicant1DQ(applicant1DQ);
        caseData.setRespondent1DQ(respondent1DQ);

        if (addHearingDocuments) {
            caseData.setHearingDocuments(wrapElements(caseDocument));
            caseData.setHearingDocumentsWelsh(wrapElements(caseDocument));
        }

        return caseData;
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

    private void verifyDecisionPrintLetter(CaseData caseData, Party party) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            DECISION_PACK_LETTER_TYPE,
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
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfullyWhenLangFieldNotSet() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
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

    @Test
    void shouldDownloadDecisionDocumentAndPrintLetterSuccessfully() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, DECISION_MADE_ON_APPLICATIONS, false, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, respondent1);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenDecisionReconsiderationOrderAbsent() {
        // given
        CaseData caseData = buildCaseData(null, SEALED_CLAIM, false, null, null, null, null);

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenDecisionReconsiderationOrderDocumentIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(new CaseDocument[] {null})) // Adding a null CaseDocument explicitly
            .build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenSystemGeneratedCaseDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null)
            .respondent1(PartyBuilder.builder().individual().build())
            .build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenSystemGeneratedCaseDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenSystemGeneratedCaseWelshDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDecisionDocument_whenSystemGeneratedCaseWelshDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToClaimantLiPSuccessfullyWhenWelsh() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS_TRANSLATED, false,  null, "WELSH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then|
        verifyDecisionPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToClaimantLiPSuccessfullyWhenBilingual() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS_TRANSLATED, false, null, "BOTH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToDefendantLiPSuccessfullyWhenBilingual() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false, "BOTH", null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToDefendantLiPSuccessfullyWhenEnglish() {
        // given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Party claimant = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false,  "ENGLISH", null, null, Language.ENGLISH);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any()))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant);
    }
}
