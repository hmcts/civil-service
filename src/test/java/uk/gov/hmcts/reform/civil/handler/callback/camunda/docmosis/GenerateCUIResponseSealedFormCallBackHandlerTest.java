package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GenerateCUIResponseSealedFormCallBackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateCUIResponseSealedFormCallBackHandler handler;
    @Mock
    private SealedClaimLipResponseFormGenerator formGenerator;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private CivilStitchService civilStitchService;
    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private AssignCategoryId assignCategoryId;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateCUIResponseSealedFormCallBackHandler(mapper, formGenerator, systemGeneratedDocumentService,
                                                                   assignCategoryId, civilStitchService, featureToggleService);
        mapper.registerModule(new JavaTimeModule());
    }

    public static final CaseDocument FORM;
    public static final CaseDocument STITCHED_DOC;
    public static final CaseDocument DIRECTIONS_QUESTIONNAIRE_DOC;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName("document name");
        document.setDocumentSize(0L);
        document.setDocumentType(DEFENDANT_DEFENCE);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);
        FORM = document;

        CaseDocument document1 = new CaseDocument();
        document1.setCreatedBy("John");
        document1.setDocumentName(String.format(N1.getDocumentTitle(), "000MC001"));
        document1.setDocumentSize(0L);
        document1.setDocumentType(DIRECTIONS_QUESTIONNAIRE);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        DIRECTIONS_QUESTIONNAIRE_DOC = document1;

        CaseDocument document2 = new CaseDocument();
        document2.setCreatedBy("John");
        document2.setDocumentName("Stitched document");
        document2.setDocumentSize(0L);
        document2.setDocumentType(SEALED_CLAIM);
        document2.setCreatedDatetime(LocalDateTime.now());
        document2.setDocumentLink(documentLink);
        STITCHED_DOC = document2;
    }

    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldGenerateForm_whenAboutToSubmitCalled() {
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingDisabled() {
        //Given
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();

        //When
        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        //Then
        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingEnabled() {
        //Given
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        ReflectionTestUtils.setField(handler, "stitchEnabled", false);
        CaseDocument doc1 = new CaseDocument();
        doc1.setDocumentName("Stitched document");

        CaseDocument doc2 = new CaseDocument();
        doc2.setDocumentName("document name");

        List<Element<CaseDocument>> documents = List.of(
                element(doc1),
                element(doc2));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class))).willReturn(documents);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(DIRECTIONS_QUESTIONNAIRE_DOC));
        CaseData caseData = CaseDataBuilder.builder()
                 .ccdCaseReference(1L)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
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

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingEnabledButRespondent1ClaimResponseTypeForSpecIsFullAdmissionSoNoDqWillGenerate() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        CaseDocument doc1 = new CaseDocument();
        doc1.setDocumentName("responseForm.pdf");
        List<Element<CaseDocument>> documents = List.of(element(doc1));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class))).willReturn(documents);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);

        //When
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        //Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals("responseForm.pdf")).count()).isEqualTo(1);

        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotCreateDuplicateDocuments_whenStitchingEnabledAndStitchedDocumentCreated() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);

        CaseDocument doc1 = new CaseDocument();
        doc1.setDocumentName("Stitched document");
        List<Element<CaseDocument>> documents = List.of(element(doc1));
        given(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(any(CaseDocument.class), any(CaseData.class))).willReturn(documents);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(DEFENDANT_DEFENCE),
                                                             anyString())).thenReturn(STITCHED_DOC);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(DIRECTIONS_QUESTIONNAIRE_DOC));
        CaseData caseData = CaseDataBuilder.builder()
                 .ccdCaseReference(1L)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments).build();

        //When
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        //Then
        // Should only have the stitched document, not both stitched and original
        long stitchedDocCount = updatedData.getSystemGeneratedCaseDocuments().stream()
                .filter(doc -> doc.getValue().getDocumentName().equals(STITCHED_DOC.getDocumentName()))
                .count();
        long originalFormCount = updatedData.getSystemGeneratedCaseDocuments().stream()
                .filter(doc -> doc.getValue().getDocumentName().equals(FORM.getDocumentName()))
                .count();

        assertThat(stitchedDocCount).isEqualTo(1);
        assertThat(originalFormCount).isEqualTo(0); // Original form should NOT be added when stitching occurs

        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_whenIsLipVLipEnabledStitchingBilingual() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(formGenerator.generate(any(CaseData.class), anyString())).willReturn(FORM);
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments).build();
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName("defendant-dq-form");
        caseData.setRespondent1OriginalDqDoc(caseDocument);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(handler, "stitchEnabled", true);

        //When
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        //Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().stream()
                       .filter(caseDocumentElement -> caseDocumentElement.getValue()
                           .getDocumentName().equals("responseForm.pdf")).count()).isEqualTo(0);

        assertThat(updatedData.getPreTranslationDocuments().size()).isEqualTo(1);

        verify(formGenerator).generate(caseData, BEARER_TOKEN);
    }
}
