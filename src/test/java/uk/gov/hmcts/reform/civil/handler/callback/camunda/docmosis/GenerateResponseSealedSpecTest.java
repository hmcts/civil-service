package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimResponseFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateResponseSealedSpec.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class GenerateResponseSealedSpecTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateResponseSealedSpec handler;

    @MockBean
    private SealedClaimResponseFormGeneratorForSpec sealedClaimResponseFormGeneratorForSpec;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;

    @MockBean
    private FeatureToggleService toggleService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    private CaseData caseData;
    private static final CaseDocument SEALED_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000MC001"))
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument DIRECTIONS_QUESTIONNAIRE_DOC =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000MC001"))
            .documentSize(0L)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final CaseDocument STITCHED_DOC =
        CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    @BeforeEach
    void setup() {
        when(sealedClaimResponseFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(SEALED_FORM);
        when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(),
                                                  any(CaseData.class))).thenReturn(STITCHED_DOC);
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(DIRECTIONS_QUESTIONNAIRE_DOC));
        caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build().toBuilder()
            .specRespondent1Represented(YES)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .specResponseTimelineDocumentFiles(ResponseDocument.builder()
                                                   .file(Document.builder()
                                                       .documentUrl("fake-url")
                                                       .documentFileName("file-name")
                                                       .documentBinaryUrl("binary-url")
                                                       .build()).build())
            .respondent1SpecDefenceResponseDocument(ResponseDocument.builder().file(Document.builder()
                                                                                        .documentUrl("fake-url")
                                                                                        .documentFileName("file-name")
                                                                                        .documentBinaryUrl("binary-url")
                                                                                        .build()).build())
            .build();
    }

    @Test
    void shouldGenerateClaimForm_whenNotV1VersionAndIsPinInPostDisabled() {
        // Given: Case data with docs to stitch, stitching is enabled and isPinInPostEnabled is false
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(false);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(STITCHED_DOC));

        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostEnabled() {
        // Given: Case data with docs to stitch, stitching is enabled and isPinInPostEnabled is true
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(true);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(STITCHED_DOC));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostDisabled() {
        // Given: Case data with docs to stitch, stitching is enabled,isPinInPostEnabled is false and callback
        // version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(false);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(STITCHED_DOC));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostEnabledStitchingDisabled() {
        // Given: Case data with docs to stitch, stitching is disabled and callback version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(true);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(SEALED_FORM));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_whenNotV1VersionAndIsPinInPostDisabledStichingDisabled() {
        // Given : Case data with docs to stitch and stitching is disabled and callback version not V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(false);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(SEALED_FORM));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_V1VersionAndIsPinInPostDisabledAndStitchDisabled() {
        // Given: Case data with docs to stitch, stitching is disabled,isPinInPostEnabled is false and callback
        // version V1
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(false);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain sealed form not stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(SEALED_FORM));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldGenerateClaimForm_WhenSpecResponseTimelineDocAndRespondent1SpecDefenceResponseDocumentIsNull() {
        // Given: Case data with docs to stitch and no existing systemGeneratedCaseDocuments,
        // stitching is enabled,isPinInPostEnabled is false and callback version V1
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build().toBuilder()
            .specRespondent1Represented(YES)
            .systemGeneratedCaseDocuments(new ArrayList<>()).build();
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);
        CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_SUBMIT);
        when(toggleService.isPinInPostEnabled()).thenReturn(false);

        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then: updatedData should contain stitched doc
        assertThat(updatedData.getSystemGeneratedCaseDocuments().contains(STITCHED_DOC));
        verify(sealedClaimResponseFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequestedSpecClaim() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.handledEvents()).contains(CaseEvent.GENERATE_RESPONSE_SEALED);
    }
}
