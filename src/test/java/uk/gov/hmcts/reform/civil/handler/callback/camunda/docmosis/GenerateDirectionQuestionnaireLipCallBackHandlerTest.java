package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
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
import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateDirectionQuestionnaireLipCallBackHandler(
            mapper,
            directionQuestionnaireLipGeneratorFactory,
            systemGeneratedDocumentService,
            assignCategoryId,
            featureToggleService
        );
        mapper.registerModule(new JavaTimeModule());
    }

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
                          .documentFileName("defendant_directions_questionnaire_form")
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
        // No longer expect duplicate copy with APP1_DQ category
    }

    @Test
    void shouldGenerateFormAndStoreItIntoPreTranslationCollection_whenAboutToSubmitCalledAndClaimantBilingual() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("BOTH").build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldGenerateFormAndStoreItIntoPreTranslationCollection_whenAboutToSubmitCalledAndClaimHasEnglishParty() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("ENGLISH").build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateFormAndStoreItIntoPreTranslationCollection_whenAboutToSubmitCalledAndDefendantBilingual() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage("BOTH").build()).build()).build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalledLipVLipEnabled() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionQuestionnaireLipResponseGenerator);
        given(directionQuestionnaireLipResponseGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionQuestionnaireLipResponseGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_APP1.getValue()));
        // No longer expect duplicate copy with APP1_DQ category
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
        // No longer expect duplicate copy with APP1_DQ category
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

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled_defendantDocHideFromWelshParty() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM_DEFENDANT);
        given(featureToggleService.isWelshEnabledForMainCase()).willReturn(true);
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference("BOTH").build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData().get("respondent1OriginalDqDoc")).isNotNull();
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_DEF1.getValue()));
        verifyNoMoreInteractions(assignCategoryId);
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled_defendantDocHideFromWelshPartyForRespondent() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM_DEFENDANT);
        given(featureToggleService.isWelshEnabledForMainCase()).willReturn(true);
        CaseData caseData = CaseData.builder().caseDataLiP(CaseDataLiP.builder()
                                                               .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                                           .respondent1ResponseLanguage(
                                                                                               "BOTH")
                                                                                           .build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData().get("respondent1OriginalDqDoc")).isNotNull();
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_DEF1.getValue()));
        verifyNoMoreInteractions(assignCategoryId);
    }

    @Test
    void shouldNotCreateDuplicateDocuments_whenClaimantDQGenerated() {
        // Given
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        // Then
        assertThat(response).isNotNull();
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
        verify(systemGeneratedDocumentService).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));

        // Should only assign one category ID for claimant DQ (DQ_APP1), not create duplicate with APP1_DQ
        verify(assignCategoryId).assignCategoryIdToCaseDocument(any(), eq(DocCategory.DQ_APP1.getValue()));
        verifyNoMoreInteractions(assignCategoryId);
    }
}
