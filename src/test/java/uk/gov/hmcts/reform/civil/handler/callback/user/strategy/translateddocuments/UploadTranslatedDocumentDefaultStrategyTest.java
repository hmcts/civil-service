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

import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CLAIMANT_INTENTION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.CCJ_REQUEST_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.STANDARD_DIRECTION_ORDER;
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

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
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
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(), any(CallbackParams.class))).willReturn(documents);
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
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(), any(CallbackParams.class))).willReturn(documents);
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
    void shouldNotSetBusinessProcess_WhenDocumentTypeIsInterlocJudgment() {
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

        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().documentFileName("interlocutory_judgment.pdf").build(),
                                                                        DocumentType.INTERLOCUTORY_JUDGEMENT)));

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
        preTranslationDocuments.add(element(CaseDocument.toCaseDocument(Document.builder().build(),
                                                                        DocumentType.LIP_MANUAL_DETERMINATION)));

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
    void shouldSetBusinessProcess_WhenDocumentTypeIsJudgementByDetermination() {
        //Given
        TranslatedDocument translatedDocument1 = TranslatedDocument
            .builder()
            .documentType(CCJ_REQUEST_DETERMINATION)
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
            .isEqualTo(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN.name());
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
}
