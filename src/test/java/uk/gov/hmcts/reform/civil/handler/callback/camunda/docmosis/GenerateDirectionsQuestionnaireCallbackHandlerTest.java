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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDirectionsQuestionnaireCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
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

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), anyString())).thenReturn(DOCUMENT);
        when(directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(any(CaseData.class),
                                                                                    anyString(), anyString()
        )).thenReturn(DOCUMENT);
    }

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
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(mock(Party.class))
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondentResponseIsSame(YesOrNo.NO)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
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
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentAdmitPartOfClaimFastTrack().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(mock(Party.class))
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondentResponseIsSame(YesOrNo.NO)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
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
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
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
    void shouldAddDocumentToSystemGeneratedDocuments_whenSameLRSameResponseSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentAdmitPartOfClaimFastTrack()
            .multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim()
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondentResponseIsSame(YesOrNo.YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
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
    void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolRespondent1() {
        for (RespondentResponseTypeSpec responseType : EnumSet.of(RespondentResponseTypeSpec.FULL_DEFENCE,
                                                                  RespondentResponseTypeSpec.PART_ADMISSION)) {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseTypeForSpec(responseType)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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
    }

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolRespondent1Spec() {
        for (RespondentResponseTypeSpec responseType : EnumSet.of(RespondentResponseTypeSpec.FULL_DEFENCE,
                                                                  RespondentResponseTypeSpec.PART_ADMISSION)) {

            when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(any(CaseData.class),
                                                                                        anyString(), anyString()
            )).thenReturn(Optional.of(DOCUMENT));

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .multiPartyClaimTwoDefendantSolicitors()
                .setClaimTypeToSpecClaim()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent1DQ(Respondent1DQ.builder().build())
                .respondent1ClaimResponseTypeForSpec(responseType)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().size()).isEqualTo(1);
        }
    }

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolRespondent2() {
        for (RespondentResponseTypeSpec responseType : EnumSet.of(RespondentResponseTypeSpec.FULL_DEFENCE,
                                                                  RespondentResponseTypeSpec.PART_ADMISSION)) {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseTypeForSpec(responseType)
                .systemGeneratedCaseDocuments(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

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
    }

    @Test
    void shouldAddDocumentToSystemGeneratedDocuments_when1v2DiffSolBothRespondents() {
        when(directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(any(CaseData.class),
                                                                      anyString(), anyString()
        )).thenReturn(Optional.of(DOCUMENT));

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(mock(Party.class))
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .systemGeneratedCaseDocuments(new ArrayList<>())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getSystemGeneratedCaseDocuments()).hasSize(2);
    }

    @Test
    void shouldAssignDefendantCategoryId_whenInvokedUnspecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseDocument defendantDocument = CaseDocument.builder()
            .createdBy("John")
            .documentName("defendant")
            .documentSize(0L)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), anyString())).thenReturn(defendantDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(defendantDocument))
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQRespondent");
    }

    @Test
    void shouldAssignClaimantCategoryId_whenInvokedAndClaimantUnspecified() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "directionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
    }

    @Test
    void shouldAssignDefendantCategoryId_when1v1or1v2SameSolicitorUnspecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseDocument defendantDocument = CaseDocument.builder()
            .createdBy("John")
            .documentName("defendant")
            .documentSize(0L)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), anyString())).thenReturn(defendantDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build()))
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQRespondent");
    }

    @Test
    void shouldAssignDefendantCategoryId_whenInvokedAnd1v2DiffSolicitorUnspecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .systemGeneratedCaseDocuments(wrapElements(DOCUMENT))
            .respondent2DocumentGeneration("userRespondent2")
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "defendant2DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQRespondentTwo");
    }

    @Test
    void shouldAssignClaimantCategoryId_whenFlagNotUserRespondent2Unspecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(DOCUMENT))
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "directionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
    }

    @Test
    void shouldAssignClaimantCategoryId_whenInvokedAndClaimantSpecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .caseAccessCategory(SPEC_CLAIM)
            .systemGeneratedCaseDocuments(wrapElements(DOCUMENT))
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "directionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
    }

    @Test
    void shouldAssignDefendantCategoryId_whenInvokedSpecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseDocument defendantDocument = CaseDocument.builder()
            .createdBy("John")
            .documentName("defendant")
            .documentSize(0L)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
        when(directionsQuestionnaireGenerator.generate(any(CaseData.class), anyString())).thenReturn(defendantDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(defendantDocument))
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQRespondent");
    }

    @Test
    void shouldAssignDefendantCategoryId_1v2SameSolicitorNotSameResponseSpecified() {
        // Given
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
            .systemGeneratedCaseDocuments(wrapElements(DOCUMENT))
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(mock(Party.class))
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2DQ(Respondent2DQ.builder().build())
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondentResponseIsSame(YesOrNo.NO)
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
            "defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQRespondent");
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).containsOnly(GENERATE_DIRECTIONS_QUESTIONNAIRE);
    }

}

