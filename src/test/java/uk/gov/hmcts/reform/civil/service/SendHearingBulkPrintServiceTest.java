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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED;
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

    private static final String SDO_HEARING_PACK_LETTER_TYPE = "hearing-document-pack";
    public static final String TASK_ID_DEFENDANT = "SendHearingToDefendantLIP";
    public static final String TASK_ID_CLAIMANT = "SendHearingToClaimantLIP";
    private static final String DECISION_PACK_LETTER_TYPE = "decision-reconsider-document-pack";
    public static final String TASK_ID_DEFENDANT_DRO = "SendToDefendantLIP";
    public static final String TASK_ID_CLAIMANT_DRO = "SendDORToClaimantLIP";
    public static final String TASK_ID_DEFENDANT_HMC = "SendAutomaticHearingToDefendantLIP";
    private static final String TEST = "test";
    private static final String WELSH_TEST = "welsh-test";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST, UPLOAD_TIMESTAMP);
    private static final Document WELSH_DOCUMENT_LINK = new Document(
        "document/welsh-url",
        WELSH_TEST,
        WELSH_TEST,
        WELSH_TEST,
        WELSH_TEST,
        UPLOAD_TIMESTAMP
    );
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private CaseData buildCaseData(Party party, DocumentType documentType, boolean addHearingDocuments,
                                   String respondentResponse, String claimIssueLang, Language appDocLang, Language defDocLang) {
        Document documentLink = documentType == DECISION_MADE_ON_APPLICATIONS_TRANSLATED
            ? WELSH_DOCUMENT_LINK
            : DOCUMENT_LINK;
        final CaseDocument caseDocument = buildCaseDocument(documentType, documentLink);
        final CaseDocument welshCaseDocument = buildCaseDocument(documentType, WELSH_DOCUMENT_LINK);

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
            caseData.setHearingDocumentsWelsh(wrapElements(welshCaseDocument));
        }

        return caseData;
    }

    private CaseDocument buildCaseDocument(DocumentType documentType, Document documentLink) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(documentType);
        caseDocument.setDocumentLink(documentLink);
        return caseDocument;
    }

    private void addEnglishAndWelshDecisionDocuments(CaseData caseData) {
        caseData.setSystemGeneratedCaseDocuments(wrapElements(
            buildCaseDocument(DECISION_MADE_ON_APPLICATIONS, DOCUMENT_LINK),
            buildCaseDocument(DECISION_MADE_ON_APPLICATIONS_TRANSLATED, WELSH_DOCUMENT_LINK)
        ));
    }

    private void verifyPrintLetter(CaseData caseData, Party party, List<String> fileNames) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            SDO_HEARING_PACK_LETTER_TYPE,
            List.of(party.getPartyName()),
            fileNames
        );
    }

    private void verifyDecisionPrintLetter(CaseData caseData, Party party, List<String> fileNames) {
        verify(bulkPrintService).printLetter(
            LETTER_CONTENT,
            caseData.getLegacyCaseReference(),
            caseData.getLegacyCaseReference(),
            DECISION_PACK_LETTER_TYPE,
            List.of(party.getPartyName()),
            fileNames
        );
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        Party respondent1 = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintLetter(caseData, respondent1, List.of("test"));
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyHMC() {
        // given
        Party respondent1 = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_HMC);

        // then
        verifyPrintLetter(caseData, respondent1, List.of("test"));
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of("test"));
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenHearingOrderAbsent() {
        // given
        CaseData caseData = buildCaseData(null, SEALED_CLAIM, false, null, null, null, null);

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

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
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null)
            .respondent1(new PartyBuilder().individual().build())
            .build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDocumentAndPrintHearingNoticeLetterToClaimantLiPWhenLanguageNotSet() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of("test"));
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfullyWhenWelsh() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, "WELSH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of(WELSH_TEST));
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToClaimantLiPSuccessfullyWhenBilingual() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, null, "BOTH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of(TEST, WELSH_TEST));
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToDefendantLiPSuccessfullyWhenBilingual() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, "BOTH", null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of(TEST, WELSH_TEST));
    }

    @Test
    void shouldDownloadDocumentAndPrintWelshHearingNoticeLetterToDefendantLiPSuccessfullyWhenWelsh() {
        // given
        Party defendant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(defendant, HEARING_FORM, true, "WELSH", null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintLetter(caseData, defendant, List.of(WELSH_TEST));
    }

    @Test
    void shouldDownloadDocumentAndPrintEnglishHearingNoticeLetterToDefendantLiPSuccessfullyWhenEnglish() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, HEARING_FORM, true, "ENGLISH", null, null, Language.WELSH);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyPrintLetter(caseData, claimant, List.of(TEST));
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseWelshDocumentsIsEmpty() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(List.of()).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotDownloadDocument_whenSystemGeneratedCaseWelshDocumentsIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        sendHearingBulkPrintService.sendHearingToLIP(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT);

        // then
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintEnglishLetterToDefendantSuccessfully() {
        // given
        Party respondent1 = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(respondent1, DECISION_MADE_ON_APPLICATIONS, false, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, respondent1, List.of(TEST));
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintEnglishLetterToClaimantLiPSuccessfully() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false, null, null, null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant, List.of(TEST));
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
            .respondent1(new PartyBuilder().individual().build())
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
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToClaimantLiPSuccessfullyWhenWelsh() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS_TRANSLATED, false,  null, "WELSH", null, null);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant, List.of(WELSH_TEST));
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToClaimantLiPSuccessfullyWhenBilingual() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS_TRANSLATED, false, null, "BOTH", null, null);
        addEnglishAndWelshDecisionDocuments(caseData);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_CLAIMANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant, List.of(TEST, WELSH_TEST));
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintWelshDecisionReconsiderationLetterToDefendantLiPSuccessfullyWhenBilingual() {
        // given
        Party defendant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(defendant, DECISION_MADE_ON_APPLICATIONS, false, "BOTH", null, null, null);
        addEnglishAndWelshDecisionDocuments(caseData);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, defendant, List.of(TEST, WELSH_TEST));
    }

    @Test
    void shouldDownloadDecisionDocumentAndPrintEnglishDecisionReconsiderationLetterToDefendantLiPSuccessfullyWhenEnglish() {
        // given
        Party claimant = new PartyBuilder().soleTrader().build();
        CaseData caseData = buildCaseData(claimant, DECISION_MADE_ON_APPLICATIONS, false,  "ENGLISH", null, null, Language.ENGLISH);
        given(coverLetterAppendService.makeDocumentMailable(any(), any(), any(), any(DocumentType.class), any(CaseDocument[].class)))
            .willReturn(new ByteArrayResource(LETTER_CONTENT).getByteArray());

        // when
        sendHearingBulkPrintService.sendDecisionReconsiderationToLip(BEARER_TOKEN, caseData, TASK_ID_DEFENDANT_DRO);

        // then
        verifyDecisionPrintLetter(caseData, claimant, List.of(TEST));
    }
}
