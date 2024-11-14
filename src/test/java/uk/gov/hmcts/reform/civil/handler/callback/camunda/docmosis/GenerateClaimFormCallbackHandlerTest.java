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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleData;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.LitigantInPersonFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGenerator;
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
    private BundleCreationService bundleCreationService;
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
    private static final LocalDateTime CREATE_DATE = LocalDateTime.of(2024, 11, 13, 20, 19, 19);
    private static final CaseDocument CLAIM_FORM =
        CaseDocument.builder()
            .createdBy("John")
            .documentName(String.format(N1.getDocumentTitle(), "000DC001"))
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
            .documentName(String.format(LIP_CLAIM_FORM.getDocumentTitle(), "000DC001"))
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
            .documentName("Stitched document")
            .createdDatetime(CREATE_DATE)
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

    private static final Document STITCHED_DOC_2 =
        Document.builder()
                .documentUrl("fake-url")
                  .documentFileName("file-name")
                  .documentBinaryUrl("binary-url")
                  .build();

    private final LocalDate issueDate = now();

    @Nested
    class GenerateClaimFormOnly {

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
            handler = new GenerateClaimFormCallbackHandler(bundleCreationService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.stitchEnabled = true;
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
            assertThat(updatedData.getIssueDate()).isEqualTo(issueDate);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verifyNoInteractions(litigantInPersonFormGenerator);
            verifyNoInteractions(bundleCreationService);
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
            verifyNoInteractions(bundleCreationService);
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
            handler = new GenerateClaimFormCallbackHandler(bundleCreationService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.stitchEnabled = true;
            mapper.registerModule(new JavaTimeModule());
            when(time.now()).thenReturn(issueDate.atStartOfDay());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsOne_withLitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(bundleCreationService.createBundle(anyLong(), anyList(), anyString())).thenReturn(getBundleCreateResponse());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);

            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(bundleCreationService).createBundle(anyLong(), anyList(), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef1LitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(bundleCreationService.createBundle(anyLong(), anyList(), anyString())).thenReturn(getBundleCreateResponse());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(bundleCreationService).createBundle(anyLong(), anyList(), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andDef2LitigantInPerson() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(bundleCreationService.createBundle(anyLong(), anyList(), anyString())).thenReturn(getBundleCreateResponse());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(bundleCreationService).createBundle(anyLong(), anyList(), anyString());
        }

        @Test
        void shouldStitchClaimFormWithLipForm_whenOneVsTwo_andBothDefendantsAreLitigantInPerson() {
            when(sealedClaimFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(CLAIM_FORM);
            when(litigantInPersonFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(LIP_FORM);
            when(bundleCreationService.createBundle(anyLong(), anyList(), anyString())).thenReturn(getBundleCreateResponse());

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedNoRespondentRepresented()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getSystemGeneratedCaseDocuments().get(0).getValue()).isEqualTo(STITCHED_DOC);
            verify(sealedClaimFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(litigantInPersonFormGenerator).generate(any(CaseData.class), eq(BEARER_TOKEN));
            verify(bundleCreationService).createBundle(anyLong(), anyList(), anyString());
        }
    }

    private static BundleCreateResponse getBundleCreateResponse() {
        Bundle bundle = Bundle.builder().value(BundleDetails.builder().stitchedDocument(STITCHED_DOC_2)
                .createdOn(CREATE_DATE).fileName("Stitched document").build()).build();
        List<Bundle> bundlesList = new ArrayList<>();
        bundlesList.add(bundle);
        return BundleCreateResponse.builder().data(BundleData.builder().caseBundles(bundlesList).build()).build();
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
        private BundleCreationService bundleCreationService;
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
            handler = new GenerateClaimFormCallbackHandler(bundleCreationService, litigantInPersonFormGenerator,
                                                           sealedClaimFormGenerator, mapper, time, assignCategoryId);
            handler.stitchEnabled = false;
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
