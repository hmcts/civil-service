package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimResponseFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType.DEFENDANT_SEALED_CLAIM_FORM_FOR_LIP_VS_LR;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class GenerateResponseSealedSpecTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private GenerateResponseSealedSpec handler;

    @Mock
    private SealedClaimResponseFormGeneratorForSpec sealedClaimResponseFormGeneratorForSpec;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateResponseSealedSpec(mapper, sealedClaimResponseFormGeneratorForSpec, civilStitchService,
                                                 new AssignCategoryId(), featureToggleService
        );
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(DIRECTIONS_QUESTIONNAIRE_DOC));
        caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecRespondent1Represented(YES);
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");
        caseData.setSpecResponseTimelineDocumentFiles(documentLink);
        ResponseDocument responseDocument = new ResponseDocument();
        responseDocument.setFile(documentLink);
        caseData.setRespondent1SpecDefenceResponseDocument(responseDocument);
    }

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private CaseData caseData;

    public static final CaseDocument SEALED_FORM;
    public static final CaseDocument DIRECTIONS_QUESTIONNAIRE_DOC;
    public static final CaseDocument STITCHED_DOC;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document1 = new CaseDocument();
        document1.setCreatedBy("John");
        document1.setDocumentName(String.format(N1.getDocumentTitle(), "000MC001"));
        document1.setDocumentSize(0L);
        document1.setDocumentType(SEALED_CLAIM);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        SEALED_FORM = document1;

        CaseDocument document2 = new CaseDocument();
        document2.setCreatedBy("John");
        document2.setDocumentName(String.format(N1.getDocumentTitle(), "000MC001"));
        document2.setDocumentSize(0L);
        document2.setDocumentType(DIRECTIONS_QUESTIONNAIRE);
        document2.setCreatedDatetime(LocalDateTime.now());
        document2.setDocumentLink(documentLink);
        DIRECTIONS_QUESTIONNAIRE_DOC = document2;

        CaseDocument document3 = new CaseDocument();
        document3.setCreatedBy("John");
        document3.setDocumentName("Stitched document");
        document3.setDocumentSize(0L);
        document3.setDocumentType(SEALED_CLAIM);
        document3.setCreatedDatetime(LocalDateTime.now());
        document3.setDocumentLink(documentLink);
        STITCHED_DOC = document3;
    }

    @Test
    void shouldGenerateClaimForm_whenNotV1VersionAndIsPinInPostDisabled() {
        // Given: Case data with docs to stitch, stitching is enabled and isPinInPostEnabled is false
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(STITCHED_DOC.getDocumentName())).count()).isEqualTo(1);

        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostEnabled() {
        // Given: Case data with docs to stitch, stitching is enabled and isPinInPostEnabled is true
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);

        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(STITCHED_DOC.getDocumentName())).count()).isEqualTo(1);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));

    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostDisabled() {
        // Given: Case data with docs to stitch, stitching is enabled,isPinInPostEnabled is false and callback
        // version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(STITCHED_DOC.getDocumentName())).count()).isEqualTo(1);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostEnabledStitchingDisabled() {
        // Given: Case data with docs to stitch, stitching is disabled and callback version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(SEALED_FORM.getDocumentName())).count()).isEqualTo(2);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_whenNotV1VersionAndIsPinInPostDisabledStichingDisabled() {
        // Given : Case data with docs to stitch and stitching is disabled and callback version not V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(SEALED_FORM.getDocumentName())).count()).isEqualTo(2);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostDisabledAndStitchDisabled() {
        // Given: Case data with docs to stitch, stitching is disabled,isPinInPostEnabled is false and callback
        // version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(SEALED_FORM.getDocumentName())).count()).isEqualTo(2);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_WhenSpecResponseTimelineDocAndRespondent1SpecDefenceResponseDocumentIsNull() {
        // Given: Case data with docs to stitch and no existing systemGeneratedCaseDocuments,
        // stitching is enabled,isPinInPostEnabled is false and callback version V1
        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        localCaseData.setSpecRespondent1Represented(YES);
        localCaseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, localCaseData, ABOUT_TO_SUBMIT);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals(STITCHED_DOC.getDocumentName())).count()).isEqualTo(1);
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldAssignCategoryId_whenInvoked() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setSystemGeneratedCaseDocuments(wrapElements(caseDocument));
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo("defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getDuplicateSystemGeneratedCaseDocs().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQRespondent");

    }

    @Test
    void shouldAssignCategoryId_whenInvokedForLip_Vs_Lr_Welsh() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        when(sealedClaimResponseFormGeneratorForSpec.generate(
            any(CaseData.class),
            anyString()
        )).thenReturn(SEALED_FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setRespondent1OriginalDqDoc(caseDocument);
        localCaseData.setClaimantBilingualLanguagePreference("BOTH");
        localCaseData.setRespondent1Represented(YES);
        localCaseData.setApplicant1Represented(NO);
        localCaseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getPreTranslationDocuments().get(0).getValue().getDocumentLink()
                       .getCategoryID()).isEqualTo("defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(DEFENDANT_SEALED_CLAIM_FORM_FOR_LIP_VS_LR);
    }

    @Test
    void shouldAssignCategoryId_whenInvokedStitchedEnabled() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setSystemGeneratedCaseDocuments(wrapElements(caseDocument));
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo("defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getDuplicateSystemGeneratedCaseDocs().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQRespondent");
    }

    @Test
    void shouldAssignCategoryId_whenInvokedStitchedEnabledForLipVSLR_Welsh() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        when(sealedClaimResponseFormGeneratorForSpec.generate(
            any(CaseData.class),
            anyString()
        )).thenReturn(SEALED_FORM);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setRespondent1OriginalDqDoc(caseDocument);
        localCaseData.setClaimantBilingualLanguagePreference("BOTH");
        localCaseData.setRespondent1Represented(YES);
        localCaseData.setApplicant1Represented(NO);
        localCaseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString()
        )).thenReturn(STITCHED_DOC);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getPreTranslationDocuments().get(0).getValue().getDocumentLink()
                       .getCategoryID()).isEqualTo("defendant1DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(DEFENDANT_SEALED_CLAIM_FORM_FOR_LIP_VS_LR);
    }

    @Test
    void shouldAssignCategoryId_whenInvokedAndRespondent2() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setSystemGeneratedCaseDocuments(wrapElements(caseDocument));
        localCaseData.setRespondent2DocumentGeneration("userRespondent2");
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo("defendant2DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getDuplicateSystemGeneratedCaseDocs().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQRespondentTwo");
    }

    @Test
    void shouldAssignCategoryId_whenInvokedAndRespondent2StitchedEnabled() {
        // Given
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);

        CaseData localCaseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(SEALED_CLAIM);
        localCaseData.setSystemGeneratedCaseDocuments(wrapElements(caseDocument));
        localCaseData.setRespondent2DocumentGeneration("userRespondent2");
        CallbackParams params = callbackParamsOf(localCaseData, ABOUT_TO_SUBMIT);
        // When
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo("defendant2DefenseDirectionsQuestionnaire");
        assertThat(updatedData.getDuplicateSystemGeneratedCaseDocs().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQRespondentTwo");
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequestedSpecClaim() {
        assertThat(handler.handledEvents()).contains(CaseEvent.GENERATE_RESPONSE_SEALED);
    }
}
