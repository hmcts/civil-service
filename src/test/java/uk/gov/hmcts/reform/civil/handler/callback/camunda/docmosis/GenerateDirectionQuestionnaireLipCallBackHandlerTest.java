package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionQuestionnaireLipGeneratorFactory;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireLipGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;

@ExtendWith(MockitoExtension.class)
class GenerateDirectionQuestionnaireLipCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DirectionQuestionnaireLipGeneratorFactory directionQuestionnaireLipGeneratorFactory;

    @Mock
    private DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    @Mock
    private AssignCategoryId assignCategoryId;

    @InjectMocks
    private GenerateDirectionQuestionnaireLipCallBackHandler handler;

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

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWithFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        verify(directionsQuestionnaireLipGenerator, never()).generate(any(CaseData.class), anyString());
        verify(systemGeneratedDocumentService, never()).getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class));
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWhenClaimantAcceptThePartAdmit() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

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

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_whenAboutToSubmitCalledWithFullAdmissionWithDefendantDoc() {
        given(directionQuestionnaireLipGeneratorFactory.getDirectionQuestionnaire()).willReturn(directionsQuestionnaireLipGenerator);
        given(directionsQuestionnaireLipGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM_DEFENDANT);
        CaseData caseData = CaseData.builder().build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(directionsQuestionnaireLipGenerator).generate(caseData, BEARER_TOKEN);
    }
}
