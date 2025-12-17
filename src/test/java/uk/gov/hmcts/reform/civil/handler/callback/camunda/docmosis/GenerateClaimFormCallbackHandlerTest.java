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
import org.springframework.test.context.NestedTestConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGenerator;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@ExtendWith(MockitoExtension.class)
class GenerateClaimFormCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private Time time;
    @Mock
    private CivilStitchService civilStitchService;
    @Mock
    private LitigantInPersonFormGenerator litigantInPersonFormGenerator;
    @Mock
    private SealedClaimFormGenerator sealedClaimFormGenerator;

    @InjectMocks
    private GenerateClaimFormCallbackHandler handler;

    @Mock
    private ObjectMapper mapper;
    @Mock
    private AssignCategoryId assignCategoryId;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    public static final CaseDocument CLAIM_FORM;
    public static final CaseDocument LIP_FORM;
    public static final CaseDocument STITCHED_DOC;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document = new CaseDocument();
        document.setCreatedBy("John");
        document.setDocumentName(String.format(N1.getDocumentTitle(), "000DC001"));
        document.setDocumentSize(0L);
        document.setDocumentType(SEALED_CLAIM);
        document.setCreatedDatetime(LocalDateTime.now());
        document.setDocumentLink(documentLink);
        CLAIM_FORM = document;

        CaseDocument document1 = new CaseDocument();
        document1.setCreatedBy("John");
        document1.setDocumentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000DC001"));
        document1.setDocumentSize(0L);
        document1.setDocumentType(LITIGANT_IN_PERSON_CLAIM_FORM);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        LIP_FORM = document1;

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

    @Nested
    class GenerateClaimFormOnly {

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
            handler = new GenerateClaimFormCallbackHandler(civilStitchService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.setStitchEnabled(true);
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            when(time.now()).thenReturn(issueDate.atStartOfDay());
        }

        @Test
        void shouldGenerateClaimForm_whenOneVsOne_andDefendantRepresented() {
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0)
                           .getValue().getDocumentLink().getCategoryID()).isEqualTo(DocCategory.CLAIMANT1_DETAILS_OF_CLAIM.getValue());
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldGenerateClaimForm_whenOneVsTwo_andBothPartiesRepresented() {
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(civilStitchService);
        }

        @Test
        void shouldGenerateClaimForm_andAssignCategoryId() {
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("detailsOfClaim");

        }
    }

    @Nested
    class GenerateAndStitchLitigantInPersonForm {

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
            handler = new GenerateClaimFormCallbackHandler(civilStitchService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.setStitchEnabled(true);
            mapper.registerModule(new JavaTimeModule());
            when(time.now()).thenReturn(issueDate.atStartOfDay());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsOne_withLitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                                 anyString())).thenReturn(STITCHED_DOC);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilStitchService).generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef1LitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                                 anyString())).thenReturn(STITCHED_DOC);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilStitchService).generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef2LitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                                 anyString())).thenReturn(STITCHED_DOC);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilStitchService).generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andBothDefendantsAreLitigantInPerson() {
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM),
                                                                 anyString())).thenReturn(STITCHED_DOC);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedNoRespondentRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(civilStitchService).generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SEALED_CLAIM), anyString());
        }
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateClaimForm");
    }

    @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
    @Nested
    @ExtendWith(MockitoExtension.class)
    class GenerateSealedClaimNoStitch {

        @Mock
        private Time time;
        @Mock
        private CivilStitchService civilStitchService;
        @Mock
        private LitigantInPersonFormGenerator litigantInPersonFormGenerator;
        @Mock
        private SealedClaimFormGenerator sealedClaimFormGenerator;

        @InjectMocks
        private GenerateClaimFormCallbackHandler handler;

        @InjectMocks
        private AssignCategoryId assignCategoryId;

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
            handler = new GenerateClaimFormCallbackHandler(civilStitchService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.setStitchEnabled(false);
            mapper.registerModule(new JavaTimeModule());
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(time.now()).thenReturn(issueDate.atStartOfDay());
        }

        @Test
        void testSingleSealedClaimGeneratedWhenStitchingDisabled() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(CLAIM_FORM);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
        }

    }
}
