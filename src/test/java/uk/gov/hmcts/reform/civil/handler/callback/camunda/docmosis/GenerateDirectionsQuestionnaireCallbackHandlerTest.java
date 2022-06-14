package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionsQuestionnaireCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GenerateDirectionsQuestionnaireCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final CaseDocument DOCUMENT = CaseDocument.builder()
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

    @MockBean
    private DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator;

    @Autowired
    private GenerateDirectionsQuestionnaireCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), anyString())).thenReturn(DOCUMENT);
        when(directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(any(CaseData.class),
                                                                                    anyString(), anyString()
        )).thenReturn(DOCUMENT);
    }

    @Nested
    class Version0 {
        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRDiffResponseRespondent1DQ() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generateDQFor1v2SingleSolDiffResponse(
                caseData, "BEARER_TOKEN", "ONE"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRDiffResponseRespondent2DQ() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentAdmitPartOfClaimFastTrack()
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.NO)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generateDQFor1v2SingleSolDiffResponse(
                caseData, "BEARER_TOKEN", "TWO"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRSameResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.YES)
                .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }
    }

    @Nested
    class Version1 {
        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            //Indu commented out
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRDiffResponseRespondent1DQ() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generateDQFor1v2SingleSolDiffResponse(
                caseData, "BEARER_TOKEN", "ONE"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRDiffResponseRespondent2DQ() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.NO)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generateDQFor1v2SingleSolDiffResponse(
                caseData, "BEARER_TOKEN", "TWO"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRSameResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentAdmitPartOfClaimFastTrack().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondentResponseIsSame(YesOrNo.YES)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionsQuestionnaireGenerator).generate(caseData, "BEARER_TOKEN");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue()).isEqualTo(DOCUMENT);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolRespondent1() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            CaseDocument generatedDocument = mock(CaseDocument.class);
            when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                caseData,
                params.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                "ONE"
            )).thenReturn(Optional.of(generatedDocument));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isEqualTo(1);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolRespondent2() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            CaseDocument generatedDocument = mock(CaseDocument.class);
            when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                caseData,
                params.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                "TWO"
            )).thenReturn(Optional.of(generatedDocument));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isEqualTo(1);
        }

        @Test
        void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolBothRespondents() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);

            CaseDocument generatedDocument1 = mock(CaseDocument.class);
            when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                caseData,
                params.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                "ONE"
            )).thenReturn(Optional.of(generatedDocument1));

            CaseDocument generatedDocument2 = mock(CaseDocument.class);
            when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                caseData,
                params.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                "TWO"
            )).thenReturn(Optional.of(generatedDocument2));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isEqualTo(2);
        }
    }
}
