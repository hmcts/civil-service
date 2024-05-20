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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateCUIResponseSealedFormCallBackHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
public class GenerateCUIResponseSealedFormCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateCUIResponseSealedFormCallBackHandler handler;
    @MockBean
    private SealedClaimLipResponseFormGenerator formGenerator;

    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;
    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private AssignCategoryId assignCategoryId;

    private static final CaseDocument FORM =
            CaseDocument.builder()
                    .createdBy("John")
                    .documentName("document name")
                    .documentSize(0L)
                    .documentType(DEFENDANT_DEFENCE)
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
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @BeforeEach
    void setup() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
    }

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingDisabled() {
        //Given
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder().build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        //When
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        //Then
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingEnabled() {
        //Given
        when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(), any(CaseData.class)))
                .thenReturn(STITCHED_DOC);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(DIRECTIONS_QUESTIONNAIRE_DOC));
        CaseData caseData = CaseDataBuilder.builder()
                .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);

        //When
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        //Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue()
                        .getDocumentName().equals(STITCHED_DOC.getDocumentName())).count()).isEqualTo(1);

        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }
}
