package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionQuestionnaireLipCallBackHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class GenerateDirectionQuestionnaireLipCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDirectionQuestionnaireLipCallBackHandler handler;
    @MockBean
    private DirectionQuestionnaireLipGeneratorFactory directionQuestionnaireLipGeneratorFactory;
    @MockBean
    private DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;
    @MockBean
    private DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;
    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private AssignCategoryId assignCategoryId;

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
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

    @BeforeEach
    void setUp() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionsQuestionnaireLipGenerator);
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalledLipVLipEnabled() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(
            directionQuestionnaireLipResponseGenerator);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        given(directionQuestionnaireLipResponseGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionQuestionnaireLipResponseGenerator).generate(caseData, BEARER_TOKEN);
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
        verify(
            systemGeneratedDocumentService,
            never()
        ).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
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
        verify(
            systemGeneratedDocumentService,
            never()
        ).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalledWhenClaimantRejectsThePartAdmit() {
        // Given
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .build();

        // Call the handler's callback method
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        // Verify interactions
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWithFullAdmissionWithDefendantDoc() {
        // Given
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString()))
            .willReturn(FORM_DEFENDANT);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }
}
