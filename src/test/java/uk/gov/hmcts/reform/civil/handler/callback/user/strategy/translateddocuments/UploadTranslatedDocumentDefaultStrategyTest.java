package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.ORDER_NOTICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadTranslatedDocumentDefaultStrategy.class,
    JacksonAutoConfiguration.class,
})
class UploadTranslatedDocumentDefaultStrategyTest {

    private static final String FILE_NAME_1 = "Some file 1";
    private static final String FILE_NAME_2 = "Some file 2";

    @Autowired
    private UploadTranslatedDocumentDefaultStrategy uploadTranslatedDocumentDefaultStrategy;

    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

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
}
