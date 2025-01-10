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

    private static final CaseDocument CLAIM_FORM =
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
                .atStatePendingClaimIssued().build().toBuilder()
                .specRespondent1Represented(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            // Then
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);

            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldGenerateClaimForm_whenOneVsTwo_andBothPartiesRepresentedSpecClaim() {
            when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
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
                .atStatePendingClaimIssued().build().toBuilder()
                .build();
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
                .atStatePendingClaimIssued().build().toBuilder()
                .specRespondent1Represented(YES)
                .specClaimTemplateDocumentFiles(new Document("fake-url",
                                                             "binary-url",
                                                             "file-name",
                                                             null, null, null))

                .specClaimDetailsDocumentFiles(new Document("fake-url",
                                                            "binary-url",
                                                            "file-name",
                                                            null, null, null))
                .build();
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
            .atStatePendingClaimIssued().build().toBuilder()
            .specRespondent1Represented(YES)
            .build();
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
            .atStatePendingClaimIssued().build().toBuilder()
            .specClaimDetailsDocumentFiles(testDocument)
            .specRespondent1Represented(YES)
            .build();
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
            .atStatePendingClaimIssued().build().toBuilder()
            .specClaimTemplateDocumentFiles(testDocument)
            .specRespondent1Represented(YES)
            .build();
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
            .atStatePendingClaimIssued().build().toBuilder()
            .specClaimDetailsDocumentFiles(testDocument)
            .specClaimTemplateDocumentFiles(testDocument)
            .specRespondent1Represented(YES)
            .build();
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
            .atStatePendingClaimIssued().build().toBuilder()
            .specClaimDetailsDocumentFiles(testDocument)
            .specClaimTemplateDocumentFiles(testDocument)
            .specRespondent1Represented(YES)
            .build();
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
                .atStatePendingClaimIssuedUnrepresentedDefendant().build().toBuilder()
                .specRespondent1Represented(NO)
                .build();
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
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented().build().toBuilder()
                .specRespondent1Represented(NO)
                .specRespondent2Represented(YES)
                .build();
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
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                .specRespondent1Represented(YES)
                .specRespondent2Represented(NO)
                .build();
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
                .atStateClaimSubmittedNoRespondentRepresented().build().toBuilder()
                .specRespondent1Represented(NO)
                .specRespondent2Represented(NO)
                .build();
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
                .atStatePendingClaimIssuedUnrepresentedDefendant().build().toBuilder()
                .specRespondent1Represented(NO)
                .build()
                .toBuilder()
                .applicant1Represented(NO)
                .build();
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
