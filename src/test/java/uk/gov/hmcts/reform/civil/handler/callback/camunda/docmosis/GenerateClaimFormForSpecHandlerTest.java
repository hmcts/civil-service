package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateClaimFormForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
public class GenerateClaimFormForSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateClaimFormForSpecCallbackHandler handler;

    @MockBean
    private SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private  AssignCategoryId assignCategoryId;

    @MockBean
    private Time time;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private CivilDocumentStitchingService civilDocumentStitchingService;

    @MockBean
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;

    @MockBean
    private FeatureToggleService toggleService;

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

    private static final CaseDocument LIP_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000MC001"))
            .documentSize(0L)
            .documentType(LITIGANT_IN_PERSON_CLAIM_FORM)
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

    @BeforeEach
    void setup() {
        when(sealedClaimFormGeneratorForSpec.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
        when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
        when(civilDocumentStitchingService.bundle(ArgumentMatchers.anyList(), anyString(), anyString(), anyString(),
                                                  any(CaseData.class))).thenReturn(STITCHED_DOC);
        when(time.now()).thenReturn(issueDate.atStartOfDay());
        documents.add(new DocumentMetaData(CLAIM_FORM.getDocumentLink(),
                                                      "Sealed Claim form",
                                                      LocalDate.now().toString()));
        documents.add(new DocumentMetaData(LIP_FORM.getDocumentLink(),
                                                      "Litigant in person claim form",
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

    }

    @Nested
    class GenerateClaimFormOnlySpec {

        @Test
        void shouldGenerateClaimForm_whenOneVsOne_andDefendantRepresentedSpecClaim() {
            // Given
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
            verifyNoInteractions(civilDocumentStitchingService);
        }

        @Test
        void shouldGenerateClaimForm_whenOneVsTwo_andBothPartiesRepresentedSpecClaim() {
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
            verifyNoInteractions(civilDocumentStitchingService);
        }

        @Test
        void shouldGenerateClaimFormWithClaimTimeLineDocs_whenUploadedByRespondent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build().toBuilder()
                .specRespondent1Represented(YES)
                .specClaimTemplateDocumentFiles(new Document("fake-url",
                                                             "binary-url",
                                                             "file-name",
                                                             null, null))

                .specClaimDetailsDocumentFiles(new Document("fake-url",
                                                             "binary-url",
                                                             "file-name",
                                                             null, null))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            specClaimTimelineDocuments.get(0).getDocument().setCategoryID(null);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(specClaimTimelineDocuments), anyString(), anyString(),
                                                         anyString(), eq(caseData));
        }

        @Test
        void shouldNotStitchClaimFormWithLipForm_whenOneVsOne_withLitigantInPersonSpecClaim_whenToggleIsOff() {
            when(toggleService.isNoticeOfChangeEnabled()).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant().build().toBuilder()
                .specRespondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
        }

    }

    @Test
    void shouldAssignCategoryId_whenInvoked() {
        // Given
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0).getValue().getCategoryID()).isEqualTo("detailsOfClaim");
    }

    @Test
    void shouldAssignCategoryIdTimeline_whenInvoked() {
        // Given
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        assertThat(updatedData.getServedDocumentFiles().getTimelineEventUpload().get(0).getValue().getCategoryID()).isEqualTo("detailsOfClaim");
    }

    @Test
    void shouldAssignCategoryIdBothTimelineAndParticulars_whenInvoked() {
        // Given
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
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
        assertThat(updatedData.getServedDocumentFiles().getParticularsOfClaimDocument().get(0).getValue().getCategoryID()).isEqualTo("detailsOfClaim");
        assertThat(updatedData.getServedDocumentFiles().getTimelineEventUpload().get(0).getValue().getCategoryID()).isEqualTo("detailsOfClaim");

    }

    @Test
    void shouldNullDocuments_whenInvokedAndCaseFileEnabled() {
        // Given
        Document testDocument = new Document("testurl",
                                             "testBinUrl", "A Fancy Name",
                                             "hash", null);
        when(toggleService.isCaseFileViewEnabled()).thenReturn(true);
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

        @BeforeEach
        void setup() {
            when(toggleService.isNoticeOfChangeEnabled()).thenReturn(true);
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsOne_withLitigantInPersonSpecClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant().build().toBuilder()
                .specRespondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(documents), anyString(), anyString(), anyString(),
                                                         eq(caseData)
            );
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef1LitigantInPersonSpecClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented().build().toBuilder()
                .specRespondent1Represented(NO)
                .specRespondent2Represented(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(documents), anyString(), anyString(), anyString(),
                                                         eq(caseData)
            );
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef2LitigantInPersonSpecClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented().build().toBuilder()
                .specRespondent1Represented(YES)
                .specRespondent2Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(documents), anyString(), anyString(), anyString(),
                                                         eq(caseData)
            );
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andBothDefendantsAreLitigantInPersonSpecClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedNoRespondentRepresented().build().toBuilder()
                .specRespondent1Represented(NO)
                .specRespondent2Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGeneratorForSpec).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilDocumentStitchingService).bundle(eq(documents), anyString(), anyString(), anyString(),
                                                         eq(caseData)
            );
        }

        @Test
        void shouldNotGenerateClaimForm_whenLipvLipFlagIsOn() {
            given(toggleService.isLipVLipEnabled()).willReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnrepresentedDefendant().build().toBuilder()
                .specRespondent1Represented(NO)
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
