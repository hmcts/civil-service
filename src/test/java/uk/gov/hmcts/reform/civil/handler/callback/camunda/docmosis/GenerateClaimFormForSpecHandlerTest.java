package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(MockitoExtension.class)
class GenerateClaimFormForSpecHandlerTest extends BaseCallbackHandlerTest {

    protected static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";
    @InjectMocks
    private GenerateClaimFormForSpecCallbackHandler handler;

    @Mock
    private SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private  AssignCategoryId assignCategoryId;

    @Mock
    private Time time;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;

    @Mock
    private FeatureToggleService toggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateClaimFormForSpecCallbackHandler(sealedClaimFormGeneratorForSpec, mapper, time, deadlinesCalculator,
                                                              civilStitchService, assignCategoryId, toggleService);
        mapper.registerModule(new JavaTimeModule());
    }

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final CaseDocument CLAIM_FORM;
    public static final CaseDocument STITCHED_DOC;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName(String.format(N1.getDocumentTitle(), "000MC001"));
        document.setDocumentSize(0L);
        document.setDocumentType(SEALED_CLAIM);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);
        CLAIM_FORM = document;

        CaseDocument document2 = new CaseDocument();
        document2.setCreatedBy("John");
        document2.setDocumentName("Stitched document");
        document2.setDocumentSize(0L);
        document2.setDocumentType(SEALED_CLAIM);
        document2.setCreatedDatetime(LocalDateTime.now());
        document2.setDocumentLink(documentLink);
        STITCHED_DOC = document2;
    }

    private final LocalDate issueDate = now();
    List<DocumentMetaData> documents = new ArrayList<>();
    List<DocumentMetaData> specClaimTimelineDocuments = new ArrayList<>();

    @Nested
    class GenerateClaimFormOnlySpec {

        @Test
        void shouldGenerateClaimForm_whenOneVsOne_andDefendantRepresentedSpecClaim() {
            // Given
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build();
            caseData.setSpecRespondent1Represented(YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);
            assertThat(updatedData.getNextDeadline()).isEqualTo(LocalDate.of(2003, Month.APRIL, 3));

            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldGenerateClaimForm_whenOneVsTwo_andBothPartiesRepresentedSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitorsSpec()
                .atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);

            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldGenerateClaimFormWithClaimTimeLineDocs_whenUploadedByRespondent() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                                 anyString())).thenReturn(STITCHED_DOC);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build();
            caseData.setSpecRespondent1Represented(YES);
            caseData.setSpecClaimTemplateDocumentFiles(new Document("fake-url",
                                                             "binary-url",
                                                             "file-name",
                                                             null, null, null));

            caseData.setSpecClaimDetailsDocumentFiles(new Document("fake-url",
                                                            "binary-url",
                                                            "file-name",
                                                            null, null, null));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilStitchService).generateStitchedCaseDocument(anyList(),
                                                                    anyString(), anyLong(), eq(SEALED_CLAIM), anyString());
        }
    }

    @Test
    void shouldAssignCategoryId_whenInvoked() {
        // Given
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
            .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                           "Sealed Claim form",
                                           LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecRespondent1Represented(YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
        // Then
        assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("detailsOfClaim");
    }

    @Test
    void shouldAssignCategoryIdParticulars_whenInvoked() {
        // Given
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                             anyString())).thenReturn(STITCHED_DOC);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
            .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                           "Sealed Claim form",
                                           LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null, UPLOAD_TIMESTAMP);
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecClaimDetailsDocumentFiles(testDocument);
        caseData.setSpecRespondent1Represented(YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
        // Then
        assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0)
                       .getValue().getCategoryID()).isEqualTo(PARTICULARS_OF_CLAIM.getValue());
    }

    @Test
    void shouldAssignCategoryIdTimeline_whenInvoked() {
        // Given
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                             anyString())).thenReturn(STITCHED_DOC);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
            .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                           "Sealed Claim form",
                                           LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null, UPLOAD_TIMESTAMP);
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecClaimTemplateDocumentFiles(testDocument);
        caseData.setSpecRespondent1Represented(YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
        // Then
        assertThat(updatedData.getServedDocumentFiles().getTimelineEventUpload().get(0).getValue()
                       .getCategoryID()).isEqualTo(PARTICULARS_OF_CLAIM.getValue());
    }

    @Test
    void shouldAssignCategoryIdBothTimelineAndParticulars_whenInvoked() {
        // Given
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                             anyString())).thenReturn(STITCHED_DOC);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
            .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                           "Sealed Claim form",
                                           LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null, UPLOAD_TIMESTAMP);
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecClaimDetailsDocumentFiles(testDocument);
        caseData.setSpecClaimTemplateDocumentFiles(testDocument);
        caseData.setSpecRespondent1Represented(YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
        // Then
        assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0).getValue().getCategoryID()).isEqualTo(
            PARTICULARS_OF_CLAIM.getValue());
        assertThat(updatedData.getServedDocumentFiles().getTimelineEventUpload().get(0).getValue().getCategoryID()).isEqualTo(
            PARTICULARS_OF_CLAIM.getValue());

    }

    @Test
    void shouldNullDocuments_whenInvokedAndCaseFileEnabled() {
        // Given
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                             anyString())).thenReturn(STITCHED_DOC);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
            .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                           "Sealed Claim form",
                                           LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Sealed Claim form",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Claim timeline",
                                                            LocalDate.now().toString()));
        specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                            "Supported docs",
                                                            LocalDate.now().toString()));
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null, UPLOAD_TIMESTAMP);
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued().build();
        caseData.setSpecClaimDetailsDocumentFiles(testDocument);
        caseData.setSpecClaimTemplateDocumentFiles(testDocument);
        caseData.setSpecRespondent1Represented(YES);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
        // Then
        assertThat(updatedData.getSpecClaimDetailsDocumentFiles()).isNull();
        assertThat(updatedData.getSpecClaimTemplateDocumentFiles()).isNull();

    }

    @Nested
    class GenerateAndStitchLitigantInPersonFormSpec {

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsOne_withLitigantInPersonSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant().build();
            caseData.setSpecRespondent1Represented(NO)
;
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentType())
                .isEqualTo(STITCHED_DOC.getDocumentType());
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef1LitigantInPersonSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented().build();
            caseData.setSpecRespondent1Represented(NO);
            caseData.setSpecRespondent2Represented(YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentType())
                .isEqualTo(STITCHED_DOC.getDocumentType());
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef2LitigantInPersonSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build();
            caseData.setSpecRespondent1Represented(YES);
            caseData.setSpecRespondent2Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentType())
                .isEqualTo(STITCHED_DOC.getDocumentType());
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andBothDefendantsAreLitigantInPersonSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(deadlinesCalculator.plus28DaysNextWorkingDayAt4pmDeadline(any()))
                .thenReturn(LocalDateTime.of(2003, Month.APRIL, 3, 2, 1));
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedNoRespondentRepresented().build();
            caseData.setSpecRespondent1Represented(NO);
            caseData.setSpecRespondent2Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentType())
                .isEqualTo(STITCHED_DOC.getDocumentType());
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldNotGenerateClaimForm_whenLipvLipFlagIsOnAndApplicantIsLip() {
            documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                               "Sealed Claim form",
                                               LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Sealed Claim form",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Claim timeline",
                                                                LocalDate.now().toString()));
            specClaimTimelineDocuments.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                                "Supported docs",
                                                                LocalDate.now().toString()));
            given(toggleService.isLipVLipEnabled()).willReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant().build();
            caseData.setSpecRespondent1Represented(NO);
            caseData.setApplicant1Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).isNull();

        }
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequestedSpecClaim() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateClaimFormForSpec");
    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(GENERATE_CLAIM_FORM_SPEC);
    }
}
