package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList.NO;
import static uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList.YES;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CLAIMANT_INTENTION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class UploadTranslatedDocumentDefaultStrategyTest {

    private static final String FILE_NAME_1 = "claimant.pdf";
    private static final String FILE_NAME_2 = "defendant.txt";

    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AssignCategoryId assignCategoryId;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        uploadTranslatedDocumentDefaultStrategy = new UploadTranslatedDocumentDefaultStrategy(systemGeneratedDocumentService,
                                                                                              objectMapper, assignCategoryId,
                                                                                              featureToggleService,
                                                                                              deadlinesCalculator
        );
    }

    @Test
    void shouldReturnDocumentListWithTranslatedDocument() {
        //Given
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(DEFENDANT_RESPONSE);
        Document document = new Document();
        document.setDocumentFileName(FILE_NAME_1);
        translatedDocument1.setFile(document);
        TranslatedDocument translatedDocument2 = new TranslatedDocument();
        translatedDocument2.setDocumentType(DEFENDANT_RESPONSE);
        Document document1 = new Document();
        document1.setDocumentFileName(FILE_NAME_2);
        translatedDocument2.setFile(document1);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(LocalDateTime.now()
                                                                                               .plusDays(28));
        CaseData caseData = CaseDataBuilder
            .builder()
            .atStatePendingClaimIssued().build();
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        CaseDocument caseDocument1 = new CaseDocument();
        caseDocument1.setDocumentName(FILE_NAME_1);
        CaseDocument caseDocument2 = new CaseDocument();
        caseDocument2.setDocumentName(FILE_NAME_2);
        List<Element<CaseDocument>> documents = List.of(
            element(caseDocument1),
            element(caseDocument2));
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        //When
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .isNotNull();

    }

    @Test
    void shouldReturnDocumentListWithTranslatedDocumentWithPreTranslatedDocumentsAdded() {
        //Given
        Document document1 = new Document();
        document1.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(CLAIMANT_INTENTION);
        translatedDocument1.setFile(document1);

        Document documentLink = new Document();
        documentLink.setDocumentFileName("claimant_response.pdf");
        documentLink.setCategoryID("aapId");
        CaseDocument originalDocument = new CaseDocument();
        originalDocument.setDocumentType(DocumentType.CLAIMANT_DEFENCE);
        originalDocument.setDocumentLink(documentLink);
        originalDocument.setDocumentName("claimant_response.pdf");

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        CaseDataBuilder
                .builder()
                .atStatePendingClaimIssued()
                .build();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        CaseDataLiP  caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        List<Element<CaseDocument>> preTranslatedDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));
        caseData.setPreTranslationDocuments(preTranslatedDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        CaseDocument doc1 = new CaseDocument();
        doc1.setDocumentName(FILE_NAME_1);
        CaseDocument doc2 = new CaseDocument();
        doc2.setDocumentName("claimant_response.pdf");
        CaseDocument doc3 = new CaseDocument();
        doc3.setDocumentName("claimant_response.pdf");
        List<Element<CaseDocument>> documents = List.of(
            element(doc1),
            element(doc2),
            element(doc3)
        );
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        //When
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .isNotNull();

    }

    @Test
    void shouldReturnExistingSystemGeneratedDocumentListWhenNothingReturnedFromService() {
        //Given
        CaseData caseData = CaseDataBuilder
                .builder()
                .atStatePendingClaimIssued()
                .build();
        TranslatedDocument translatedDoc = new TranslatedDocument();
        translatedDoc.setDocumentType(CLAIM_ISSUE);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(List.of(element(translatedDoc)));
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData()).extracting("systemGeneratedCaseDocuments")
            .isNotNull();
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipVsLipAndCcdState_In_Pending_Case_Issued_R2FlagEnabled() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO).build();
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        caseData.setCcdCaseReference(123L);
        TranslatedDocument translatedDoc = new TranslatedDocument();
        translatedDoc.setDocumentType(CLAIM_ISSUE);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(List.of(element(translatedDoc)));
        caseData.setCaseDataLiP(caseDataLiP);

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
                callbackParams);
        //Then
        assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipVsLRAndCcdState_In_Awaiting_Claimant_Response_NocOnlineFlagEnabled() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse()
                .respondent1Represented(YesOrNo.YES)
                .specRespondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO).build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setCcdCaseReference(123L);
        TranslatedDocument translatedDoc = new TranslatedDocument();
        translatedDoc.setDocumentType(CLAIM_ISSUE);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(List.of(element(translatedDoc)));
        caseData.setCaseDataLiP(caseDataLiP);

        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLrVsLipAndCcdState_InAwaitingClaimantResponse() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Document doc = new Document();
        doc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(CLAIMANT_INTENTION);
        translatedDocument1.setFile(doc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document docForPreTranslation = new Document();
        docForPreTranslation.setDocumentFileName("claimant.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            docForPreTranslation,
            DocumentType.CLAIMANT_INTENTION_TRANSLATED_DOCUMENT
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setCcdCaseReference(123L);
        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .isNotNull();
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_LR_INTENTION.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentTypeIsOrderNotice_ToggleEnabledCP() {
        //Given
        Document orderDoc = new Document();
        orderDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(ORDER_NOTICE);
        translatedDocument1.setFile(orderDoc);
        Document doc2 = new Document();
        doc2.setDocumentFileName(FILE_NAME_2);
        TranslatedDocument translatedDocument2 = new TranslatedDocument();
        translatedDocument2.setDocumentType(ORDER_NOTICE);
        translatedDocument2.setFile(doc2);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsStandardDirectionOrder() {
        //Given
        Document sdoFileDoc = new Document();
        sdoFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(STANDARD_DIRECTION_ORDER);
        translatedDocument1.setFile(sdoFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document sdoDoc = new Document();
        sdoDoc.setDocumentFileName("standard_direction_order.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            sdoDoc,
            DocumentType.SDO_ORDER
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SDO.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsInterlocJudgment() {
        //Given
        Document interlocFileDoc = new Document();
        interlocFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(INTERLOCUTORY_JUDGMENT);
        translatedDocument1.setFile(interlocFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();

        Document interlocDoc = new Document();
        interlocDoc.setDocumentFileName("interlocutory_judgment.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            interlocDoc,
            DocumentType.INTERLOCUTORY_JUDGEMENT
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsManualDetermination() {
        //Given
        Document manualFileDoc = new Document();
        manualFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(MANUAL_DETERMINATION);
        translatedDocument1.setFile(manualFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document manualDoc = new Document();
        manualDoc.setDocumentFileName("manual_determination.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            manualDoc,
            DocumentType.LIP_MANUAL_DETERMINATION
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsFinalOrder() {
        //Given
        Document finalOrderFileDoc = new Document();
        finalOrderFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(FINAL_ORDER);
        translatedDocument1.setFile(finalOrderFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document finalOrderDoc = new Document();
        finalOrderDoc.setDocumentFileName("final_order.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            finalOrderDoc,
            DocumentType.JUDGE_FINAL_ORDER
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setFinalOrderDocumentCollection(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_ORDER.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsSettlementAgreement() {

        //Given
        Document settlementFileDoc = new Document();
        settlementFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(SETTLEMENT_AGREEMENT);
        translatedDocument1.setFile(settlementFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document settlementDoc = new Document();
        settlementDoc.setDocumentFileName("settlement_agreement.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            settlementDoc,
            DocumentType.SETTLEMENT_AGREEMENT)));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT.name());

    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsDecisionMadeOnApplications() {
        //Given
        Document decisionFileDoc = new Document();
        decisionFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(DECISION_MADE_ON_APPLICATIONS);
        translatedDocument1.setFile(decisionFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document decisionDoc = new Document();
        decisionDoc.setDocumentFileName("decision_made.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            decisionDoc,
            DocumentType.DECISION_MADE_ON_APPLICATIONS)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsSdo() {
        //Given
        Document sdoFileDoc = new Document();
        sdoFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(STANDARD_DIRECTION_ORDER);
        translatedDocument1.setFile(sdoFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document ccjDoc = new Document();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(ccjDoc,
                                                                        DocumentType.CCJ_REQUEST_DETERMINATION)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_SDO.name());
    }

    @Test
    void shouldCopyOtherDocumentExceptSealedClaimForm() {
        //Given
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        Document claimIssueFileDoc = new Document();
        claimIssueFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(CLAIM_ISSUE);
        translatedDocument1.setFile(claimIssueFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document claimantDqDoc = new Document();
        claimantDqDoc.setDocumentFileName("claimant_dq.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            claimantDqDoc,
            DocumentType.CLAIMANT_CLAIM_FORM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build();
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE.name());
    }

    @Test
    void shouldNotCopySealedClaimForm() {
        //Given
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        CaseDocument sealedClaimDoc = new CaseDocument();
        sealedClaimDoc.setDocumentName("000MC001-sealed-claim-form.pdf");
        sealedClaimDoc.setDocumentType(DocumentType.SEALED_CLAIM);
        List<Element<CaseDocument>> documents = List.of(
            element(sealedClaimDoc));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        Document claimIssueFileDoc = new Document();
        claimIssueFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(CLAIM_ISSUE);
        translatedDocument1.setFile(claimIssueFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document sealedFormDoc = new Document();
        sealedFormDoc.setDocumentFileName("sealed_form.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            sealedFormDoc,
            DocumentType.SEALED_CLAIM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build();
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        //Then
        assertThat(documentsList.size()).isEqualTo(1);
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE.name());
    }

    @Test
    void shouldCopySealedClaimForm() {
        //Given
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        CaseDocument sealedClaimFormDoc = new CaseDocument();
        sealedClaimFormDoc.setDocumentName("000MC001-sealed-claim-form.pdf");
        sealedClaimFormDoc.setDocumentType(DocumentType.SEALED_CLAIM);
        List<Element<CaseDocument>> documents = List.of(
            element(sealedClaimFormDoc));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Document claimIssueFileDoc = new Document();
        claimIssueFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(CLAIM_ISSUE);
        translatedDocument1.setFile(claimIssueFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document sealedFormDoc = new Document();
        sealedFormDoc.setDocumentFileName("sealed_form.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            sealedFormDoc,
            DocumentType.SEALED_CLAIM
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build();
        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        //Then
        assertThat(documentsList.size()).isEqualTo(1);
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIM_ISSUE.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsCourtOfficerOrder() {
        //Given
        Document courtOfficerFileDoc = new Document();
        courtOfficerFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(COURT_OFFICER_ORDER);
        translatedDocument1.setFile(courtOfficerFileDoc);
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document courtOfficerDoc = new Document();
        courtOfficerDoc.setDocumentFileName("court_officer_order.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            courtOfficerDoc,
            DocumentType.COURT_OFFICER_ORDER
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.COURT_OFFICER_ORDER.name());
        assertThat(response.getData()).extracting("courtOfficersOrders")
            .isNotNull();
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsDefendantDefence() {
        //Given
        Document defendantResponseFileDoc = new Document();
        defendantResponseFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(DEFENDANT_RESPONSE);
        translatedDocument1.setFile(defendantResponseFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document responseSealedDoc = new Document();
        responseSealedDoc.setDocumentFileName("response_sealed_form.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            responseSealedDoc,
            DocumentType.DEFENDANT_DEFENCE
        )));
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(LocalDateTime.now()
                                                                                               .plusDays(28));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);

        assertThat(response.getData()).extracting("respondent1ClaimResponseDocumentSpec")
            .isNotNull();
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .isNotNull();
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsHearingNotice() {
        //Given
        Document hearingNoticeFileDoc = new Document();
        hearingNoticeFileDoc.setDocumentFileName(FILE_NAME_1);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(HEARING_NOTICE);
        translatedDocument1.setFile(hearingNoticeFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document hearingDoc = new Document();
        hearingDoc.setDocumentFileName("hearing_small_claim.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(hearingDoc,
                                                                        DocumentType.HEARING_FORM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
        //Then
        assertThat(updatedData.getHearingDocuments().size()).isEqualTo(1);
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentType_discontinue_claim() {
        //Given
        Document noticeDiscontinueFileDoc = new Document();
        noticeDiscontinueFileDoc.setDocumentFileName(FILE_NAME_2);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT);
        translatedDocument1.setFile(noticeDiscontinueFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document noticeDoc = new Document();
        noticeDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            noticeDoc,
            DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setCourtPermissionNeeded(NO);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DISCONTINUANCE_DOC.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentType_discontinue_claimForJudgeVerification() {
        //Given
        Document noticeDiscontinueFileDoc = new Document();
        noticeDiscontinueFileDoc.setDocumentFileName(FILE_NAME_2);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT);
        translatedDocument1.setFile(noticeDiscontinueFileDoc);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document noticeDoc = new Document();
        noticeDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            noticeDoc,
            DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setCourtPermissionNeeded(YES);
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        var caseDocument = response.getData().get("respondent1NoticeOfDiscontinueAllPartyTranslatedDoc");
        assertThat(caseDocument).isNotNull();
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentTypeDefendantResponseOfLr() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(DEFENDANT_RESPONSE);
        Document document = new Document();
        document.setDocumentFileName(FILE_NAME_2);
        translatedDocument1.setFile(document);

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document noticeSealedDoc = new Document();
        noticeSealedDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            noticeSealedDoc,
            DocumentType.SEALED_CLAIM
        )));
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any())).thenReturn(LocalDateTime.now()
                                                                                               .plusDays(28));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setClaimantBilingualLanguagePreference("BOTH");
        caseData.setApplicant1Represented(YesOrNo.NO);
        Document dqDoc = new Document();
        dqDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        caseData.setRespondent1OriginalDqDoc(CaseDocument.toCaseDocument(
                dqDoc,
                DocumentType.DIRECTIONS_QUESTIONNAIRE
            ));
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .isNotNull();
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DEFENDANT_SEALED_FORM.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentTypeDefendantResponseOfLrWhenNoTranslatedDOc() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        Document noticeDiscontinueFileDoc = new Document();
        noticeDiscontinueFileDoc.setDocumentFileName(FILE_NAME_2);
        TranslatedDocument translatedDocument1 = new TranslatedDocument();
        translatedDocument1.setDocumentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT);
        translatedDocument1.setFile(noticeDiscontinueFileDoc);
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        Document noticeSealedDoc = new Document();
        noticeSealedDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            noticeSealedDoc,
            DocumentType.SEALED_CLAIM
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setTranslatedDocuments(translatedDocument);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setPreTranslationDocuments(preTranslationDocuments);
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setClaimantBilingualLanguagePreference("BOTH");
        caseData.setApplicant1Represented(YesOrNo.NO);
        Document dqDoc = new Document();
        dqDoc.setDocumentFileName("notice_of_discontinuance.pdf");
        caseData.setRespondent1OriginalDqDoc(CaseDocument.toCaseDocument(
                dqDoc,
                DocumentType.DIRECTIONS_QUESTIONNAIRE
            ));
        caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        caseData.setCcdCaseReference(123L);

        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .isNotNull().isEmpty();

    }
}
