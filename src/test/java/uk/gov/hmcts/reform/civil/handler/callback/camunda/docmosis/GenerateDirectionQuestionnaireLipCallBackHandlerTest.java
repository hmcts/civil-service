package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionQuestionnaireLipGeneratorFactory;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionQuestionnaireLipResponseGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireLipGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GenerateDirectionQuestionnaireLipCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DirectionQuestionnaireLipGeneratorFactory directionQuestionnaireLipGeneratorFactory;

    @Mock
    private DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;

    @Mock
    private DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private GenerateDirectionQuestionnaireLipCallBackHandler handler;

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("claimant_document_name")
        .documentSize(0L)
        .documentType(DIRECTIONS_QUESTIONNAIRE)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    private static final CaseDocument FORM_DEFENDANT = CaseDocument.builder()
        .createdBy("John")
        .documentName("defendant_doc")
        .documentSize(0L)
        .documentType(DIRECTIONS_QUESTIONNAIRE)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_APP1.getValue()));
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.APP1_DQ.getValue()));
    }

    @Test
    void shouldGenerateFormAndStoreItIntoPreTranslationCollection_whenAboutToSubmitCalledAndMainClaimHasBilingualParty() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("BOTH").build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalledLipVLipEnabled() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionQuestionnaireLipResponseGenerator);
        given(directionQuestionnaireLipResponseGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionQuestionnaireLipResponseGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_APP1.getValue()));
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.APP1_DQ.getValue()));
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWithFullAdmission() {
        // Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        // Call the handler's callback method
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        // Verify interactions
        verify(directionsQuestionnaireLipGenerator, never()).generate(any(CaseData.class), anyString());
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWhenClaimantAcceptThePartAdmit() {
        // Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        // Call the handler's callback method
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        // Verify interactions
        verify(directionsQuestionnaireLipGenerator, never()).generate(any(CaseData.class), anyString());
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalledWhenClaimantRejectsThePartAdmit() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .build();

        // Call the handler's callback method
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_APP1.getValue()));
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.APP1_DQ.getValue()));
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled_defendantDoc() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM_DEFENDANT);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_DEF1.getValue()));
        verifyNoMoreInteractions(assignCategoryId);
    }
}
