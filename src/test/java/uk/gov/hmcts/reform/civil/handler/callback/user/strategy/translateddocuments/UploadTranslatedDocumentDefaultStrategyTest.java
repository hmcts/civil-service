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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

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

    private static final String FILE_NAME_1 = "claimant";
    private static final String FILE_NAME_2 = "defendant";

    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AssignCategoryId assignCategoryId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        uploadTranslatedDocumentDefaultStrategy = new UploadTranslatedDocumentDefaultStrategy(systemGeneratedDocumentService,
                                                                                              objectMapper, assignCategoryId,
                                                                                              featureToggleService);
    }

    @Test
    void shouldReturnDocumentListWithTranslatedDocument() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(DEFENDANT_RESPONSE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        TranslatedDocument translatedDocument2 = TranslatedDocument
            .builder()
            .documentType(DEFENDANT_RESPONSE)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );

        CaseData caseData = CaseDataBuilder
            .builder()
            .atStatePendingClaimIssued()
            .build()
            .builder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        List<Element<CaseDocument>> documents = List.of(
            element(CaseDocument.builder().documentName(FILE_NAME_1).build()),
            element(CaseDocument.builder().documentName(FILE_NAME_2).build()));
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        //When
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(CLAIMANT_INTENTION)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        CaseDocument originalDocument = CaseDocument
            .builder()
            .documentType(DocumentType.CLAIMANT_DEFENCE)
            .documentLink(Document.builder().documentFileName("claimant_response.pdf")
                              .categoryID("aapId").build())
            .documentName("claimant response")
            .build();

        List<Element<CaseDocument>> preTranslatedDocuments = new ArrayList<>(List.of(
            element(originalDocument)
        ));

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        CaseData caseData = CaseDataBuilder
            .builder()
            .atStatePendingClaimIssued()
            .build()
            .builder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData.toBuilder().preTranslationDocuments(preTranslatedDocuments).build()).build();
        List<Element<CaseDocument>> documents = List.of(
            element(CaseDocument.builder().documentName(FILE_NAME_1).build()),
            element(CaseDocument.builder().documentName("claimant response").build()),
            element(CaseDocument.builder().documentName("claimant response").build()));
        @SuppressWarnings("unchecked")
        List<Element<TranslatedDocument>> expectedTranslatedDocs = (List<Element<TranslatedDocument>>) any(List.class);
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        //When
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
            .build()
            .builder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdState(CaseState.PENDING_CASE_ISSUED)
                .ccdCaseReference(123L)
                .build();

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
            .build().toBuilder()
            .respondent1Represented(YesOrNo.YES)
            .specRespondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentTypeIsOrderNotice_ToggleEnabledCP() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(ORDER_NOTICE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        TranslatedDocument translatedDocument2 = TranslatedDocument
            .builder()
            .documentType(ORDER_NOTICE)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
    void shouldUpdateBusinessProcess_WhenLipIsBilingual_documentTypeIsOrderNotice_ToggleDisabledCP() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(ORDER_NOTICE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        TranslatedDocument translatedDocument2 = TranslatedDocument
            .builder()
            .documentType(ORDER_NOTICE)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1),
            element(translatedDocument2)
        );

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT.name());
    }

    @Test
    void shouldSetBusinessProcess_WhenDocumentTypeIsStandardDirectionOrder() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(STANDARD_DIRECTION_ORDER)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().documentFileName("standard_direction_order.pdf").build(),
            DocumentType.SDO_ORDER
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(INTERLOCUTORY_JUDGMENT)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();

        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().documentFileName("interlocutory_judgment.pdf").build(),
            DocumentType.INTERLOCUTORY_JUDGEMENT
        )));
      
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(MANUAL_DETERMINATION)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().build(),
            DocumentType.LIP_MANUAL_DETERMINATION
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(FINAL_ORDER)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.JUDGE_FINAL_ORDER
        )));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .finalOrderDocumentCollection(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.GENERATE_ORDER_NOTIFICATION.name());
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsSettlementAgreement() {

        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(SETTLEMENT_AGREEMENT)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();
        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.SETTLEMENT_AGREEMENT)));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(DECISION_MADE_ON_APPLICATIONS)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.DECISION_MADE_ON_APPLICATIONS)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(STANDARD_DIRECTION_ORDER)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.CCJ_REQUEST_DETERMINATION)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(CLAIM_ISSUE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.CLAIMANT_CLAIM_FORM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build().toBuilder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        List<Element<CaseDocument>> documents = List.of(
            element(CaseDocument.builder().documentName("000MC001-sealed-claim-form.pdf").documentType(DocumentType.SEALED_CLAIM).build()));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
            expectedTranslatedDocs,
            any(CaseData.class)
        )).willReturn(documents);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(CLAIM_ISSUE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.SEALED_CLAIM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .build().toBuilder()
            .ccdState(CaseState.PENDING_CASE_ISSUED)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(COURT_OFFICER_ORDER)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().documentFileName("court_officer_order.pdf").build(),
            DocumentType.COURT_OFFICER_ORDER
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        //Then
        assertThat(response.getData())
            .extracting("businessProcess")
            .extracting("camundaEvent")
            .isEqualTo(CaseEvent.COURT_OFFICER_ORDER.name());
        assertThat(response.getData()).extracting("previewCourtOfficerOrder")
            .isNotNull();
        assertThat(response.getData()).extracting("translatedCourtOfficerOrder")
            .isNotNull();
    }

    @Test
    void shouldUpdateBusinessProcess_WhenDocumentTypeIsHearingNotice() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(HEARING_NOTICE)
            .file(Document.builder().documentFileName(FILE_NAME_1).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = List.of(
            element(translatedDocument1)
        );

        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().documentFileName("hearing_small_claim.pdf").build(),
                                                                        DocumentType.HEARING_FORM)));

        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().documentFileName("notice_of_discontinuance.pdf").build(),
            DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .courtPermissionNeeded(NO)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
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
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(NOTICE_OF_DISCONTINUANCE_DEFENDANT)
            .file(Document.builder().documentFileName(FILE_NAME_2).build())
            .build();

        List<Element<TranslatedDocument>> translatedDocument = new ArrayList<>(List.of(
            element(translatedDocument1)
        ));
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(
            Document.builder().documentFileName("notice_of_discontinuance.pdf").build(),
            DocumentType.NOTICE_OF_DISCONTINUANCE_DEFENDANT
        )));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .translatedDocuments(translatedDocument)
                             .build())
            .preTranslationDocuments(preTranslationDocuments)
            .courtPermissionNeeded(YES)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .ccdCaseReference(123L)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        //When
        var response = (AboutToStartOrSubmitCallbackResponse) uploadTranslatedDocumentDefaultStrategy.uploadDocument(
            callbackParams);
        var caseDocument = response.getData().get("respondent1NoticeOfDiscontinueAllPartyTranslatedDoc");
        assertThat(caseDocument).isNotNull();
    }
}
