package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.consentorder.ConsentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.directionorder.DirectionOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DismissalOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.AssistedOrderFormGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.HearingOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationConcurrentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationSequentialOrderGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType.GENERAL_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GeneratePDFDocumentCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class},
    properties = {"print.service.enabled=true"})
class GeneratePDFDocumentCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @MockitoBean
    private Time time;

    @MockitoBean
    private GeneralOrderGenerator generalOrderGenerator;

    @MockitoBean
    private ConsentOrderGenerator consentOrderGenerator;

    @MockitoBean
    private RequestForInformationGenerator requestForInformationGenerator;

    @MockitoBean
    private DirectionOrderGenerator directionOrderGenerator;

    @MockitoBean
    private DismissalOrderGenerator dismissalOrderGenerator;

    @MockitoBean
    private HearingOrderGenerator hearingOrderGenerator;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private CoreCaseDataService coreCaseDataService;

    @MockitoBean
    private WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;

    @MockitoBean
    private WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentailOrderGenerator;

    @MockitoBean
    private FreeFormOrderGenerator freeFormOrderGenerator;

    @MockitoBean
    private AssistedOrderFormGenerator assistedOrderFormGenerator;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private GaForLipService gaForLipService;

    @MockitoBean
    private SendFinalOrderPrintService sendFinalOrderPrintService;

    @Autowired
    private GeneratePDFDocumentCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final LocalDate submittedOn = now();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(handler, "printServiceEnabled", "true");
        when(generalOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(generalOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(directionOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
        when(directionOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.DIRECTION_ORDER_DOCUMENT);

        when(dismissalOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);
        when(dismissalOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);

        when(hearingOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);
        when(hearingOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);

        when(writtenRepresentationSequentailOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
        when(writtenRepresentationSequentailOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);

        when(writtenRepresentationConcurrentOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
        when(writtenRepresentationConcurrentOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);

        when(requestForInformationGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);
        when(requestForInformationGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

        when(freeFormOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(freeFormOrderGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(assistedOrderFormGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(assistedOrderFormGenerator.generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), anyString(), any(FlowFlag.class)))
            .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(consentOrderGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.CONSENT_ORDER_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateGeneralOrderDocumentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateDirectionOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getDirectionOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldHaveListOfTwoGenerateDirectionOrderDocIfElementInListAlreadyPresent() {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                .documentLink(Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                                  .documentBinaryUrl("binaryUrl").build())
                .documentType(DocumentType.DIRECTION_ORDER).documentSize(12L).build();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .directionOrderDocument(wrapElements(caseDocument))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getDirectionOrderDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldGenerateDismissalOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().dismissalOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getDismissalOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateDismissalOrderDocument() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().dismissalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldNotPrintGenerateDismissalOrderDocument_ifApplicantHasBilingualPreference() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().dismissalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator, times(1))
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));
            verifyNoMoreInteractions(dismissalOrderGenerator);
            verifyNoInteractions(sendFinalOrderPrintService);

        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().finalOrderFreeForm()
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateFreeFormOrderDocument() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().finalOrderFreeForm()
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateHearingOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getHearingOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.HEARING_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldCopyHearingOrderDocInTempCollectionIfWelshParty() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.HEARING_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.HEARING_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateHearingOrderDocumentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldPrintGenerateWrittenRepresentationSequentialDocumentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .writtenRepresentationSequentialApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getWrittenRepSequentialDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldHaveListOfTwoGenerateWrittenRepSequentialDocIfElementInListAlreadyPresent() {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                .documentLink(Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                                  .documentBinaryUrl("binaryUrl").build())
                .documentType(DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL).documentSize(12L).build();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication()
                .writtenRepSequentialDocument(wrapElements(caseDocument))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator)
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getWrittenRepSequentialDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateWrittenRepresentationConccurentDocumentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .writtenRepresentationConcurrentApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verify(writtenRepresentationConcurrentOrderGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateWrittenRepresentationConccurentDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getWrittenRepConcurrentDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldHaveListOfTwoGenerateWrittenRepConcurrentDocIfElementInListAlreadyPresent() {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                .documentLink(Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                                  .documentBinaryUrl("binaryUrl").build())
                .documentType(DocumentType.WRITTEN_REPRESENTATION_CONCURRENT).documentSize(12L).build();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication()
                .writtenRepConcurrentDocument(wrapElements(caseDocument))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator)
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getWrittenRepConcurrentDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateRequestForInformationDocumentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .requestForInformationApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            verify(requestForInformationGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateRequestForInformationDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getRequestForInformationDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateSendAppToOtherPartyLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .build();

            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verify(requestForInformationGenerator, times(2))
                .generate(any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                                                  any(GeneralApplicationCaseData.class), any(GeneralApplicationCaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldGenerateSendAppToOtherParty_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .judgeRecitalText("test")
                                                     .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .judgeRequestMoreInfoByDate(now()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getRequestForInformationDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldHaveListOfTwoGenerateRequestForInfotDocIfElementInListAlreadyPresent() {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                .documentLink(Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                                  .documentBinaryUrl("binaryUrl").build())
                .documentType(DocumentType.REQUEST_FOR_INFORMATION).documentSize(12L).build();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .requestForInformationDocument(wrapElements(caseDocument))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator)
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getRequestForInformationDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(value = GaFinalOrderSelection.class)
        void shouldGenerateGeneralOrderDoc_whenAboutToSubmitEventIsCalled_withFinalOrder(
                GaFinalOrderSelection selection) {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                    .build()
                    .toBuilder()
                    .finalOrderSelection(selection)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(value = GaFinalOrderSelection.class)
        void shouldHaveListOfTwoGeneralOrderDocumentIfElementInListAlreadyPresent_withFinalOrder(
                GaFinalOrderSelection selection) {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                    .documentLink(Document.builder().documentUrl("url")
                            .documentFileName("filename").documentHash("hash")
                            .documentBinaryUrl("binaryUrl").build())
                    .documentType(DocumentType.GENERAL_ORDER).documentSize(12L).build();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                    .build()
                    .toBuilder()
                    .finalOrderSelection(selection)
                    .generalOrderDocument(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(value = GaFinalOrderSelection.class)
        void shouldNotPrintFinalOrderDocument_ifApplicantHasBilingualPreference(
            GaFinalOrderSelection selection) {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .build()
                .toBuilder()
                .finalOrderSelection(selection)
                .isGaApplicantLip(YesOrNo.YES)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
            verifyNoInteractions(sendFinalOrderPrintService);
        }

        @Test
        void shouldGenerateConsentOrderDocument_whenAboutToSubmitEventIsCalled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().consentOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(consentOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getConsentOrderDocument().get(0).getValue())
                .isEqualTo(PDFBuilder.CONSENT_ORDER_DOCUMENT);

        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslation() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForWelshTranslation() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaApplicantLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslation() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForApplicantWelshLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .build();

            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(DIRECTIONS_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForRespondentWelshLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .build();

            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(DIRECTIONS_ORDER_DOC);
        }

        @Test
        void shouldNotPauseGenerateDirectionOrderDocumentForLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .build();

            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForWelshTranslation() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaApplicantLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForNotRespondentWelshTranslation() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaRespondentOneLip(YesOrNo.NO)
                    .respondentBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslationWhenFlagOff() {
            GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaRespondentOneLip(YesOrNo.NO)
                    .respondentBilingualLanguagePreference(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentailOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .generalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForWelshApplicantLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().finalOrderFreeForm()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForWelshRespondentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().finalOrderFreeForm()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForNonWelshRespondentLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().finalOrderFreeForm()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.NO)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForRespondentWelshLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .generalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForNonWelshLip() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .generalOrderApplication()
                .isGaApplicantLip(YesOrNo.YES)
                .isGaRespondentOneLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .respondentBilingualLanguagePreference(YesOrNo.NO)
                .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLipButDontHideIt() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .requestForInformationApplication()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY).build())
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .applicantBilingualLanguagePreference(YES)
                .parentClaimantIsApplicant(YES)
                .build();
            caseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                                                     .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                                     .build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
            assertThat(updatedData.getRequestForInformationDocument()).isNotEmpty();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLipButHideIt() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .requestForInformationApplication()
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                    GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).judgeRequestMoreInfoByDate(LocalDate.now().minusDays(1))
                .judgeRequestMoreInfoText("test").build())
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(YES)
                .applicantBilingualLanguagePreference(YES)
                .build();
            caseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                                                                 .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                                                                 .build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isNotEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC);
            assertThat(updatedData.getRequestForInformationDocument()).isEmpty();
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("CreatePDFDocument");
        }
    }
}
