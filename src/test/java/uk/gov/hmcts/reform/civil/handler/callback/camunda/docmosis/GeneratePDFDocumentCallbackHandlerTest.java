package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.consentorder.ConsentOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.directionorder.DirectionOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dismissalorder.DismissalOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.finalorder.AssistedOrderFormGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.generalorder.GeneralOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.hearingorder.HearingOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.requestmoreinformation.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.writtenrepresentationconcurrentorder.WrittenRepresentationConcurrentOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.writtenrepresentationsequentialorder.WrittenRepresentationSequentialOrderGenerator;
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
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType.DIRECTIONS_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType.GENERAL_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType.WRITTEN_REPRESENTATION_ORDER_DOC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GeneratePDFDocumentCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class},
    properties = {"print.service.enabled=true"})
class GeneratePDFDocumentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private GeneralOrderGenerator generalOrderGenerator;

    @MockBean
    private ConsentOrderGenerator consentOrderGenerator;

    @MockBean
    private RequestForInformationGenerator requestForInformationGenerator;

    @MockBean
    private DirectionOrderGenerator directionOrderGenerator;

    @MockBean
    private DismissalOrderGenerator dismissalOrderGenerator;

    @MockBean
    private HearingOrderGenerator hearingOrderGenerator;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;

    @MockBean
    private WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentialOrderGenerator;

    @MockBean
    private FreeFormOrderGenerator freeFormOrderGenerator;

    @MockBean
    private AssistedOrderFormGenerator assistedOrderFormGenerator;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private GaForLipService gaForLipService;

    @MockBean
    private SendFinalOrderPrintService sendFinalOrderPrintService;

    @Autowired
    private GeneratePDFDocumentCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final LocalDate submittedOn = now();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(handler, "printServiceEnabled", "true");
        when(generalOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(generalOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(directionOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
        when(directionOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.DIRECTION_ORDER_DOCUMENT);

        when(dismissalOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);
        when(dismissalOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);

        when(hearingOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);
        when(hearingOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);

        when(writtenRepresentationSequentialOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
        when(writtenRepresentationSequentialOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);

        when(writtenRepresentationConcurrentOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
        when(writtenRepresentationConcurrentOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);

        when(requestForInformationGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);
        when(requestForInformationGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

        when(freeFormOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(freeFormOrderGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(assistedOrderFormGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);
        when(assistedOrderFormGenerator.generate(any(CaseData.class), any(CaseData.class), anyString(), any(FlowFlag.class)))
                .thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

        when(consentOrderGenerator.generate(any(CaseData.class), anyString()))
                .thenReturn(PDFBuilder.CONSENT_ORDER_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateGeneralOrderDocumentLip() {
            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateDirectionOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getDirectionOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForLip() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldHaveListOfTwoGenerateDirectionOrderDocIfElementInListAlreadyPresent() {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                    .documentLink(Document.builder().documentUrl("url").documentFileName("filename").documentHash("hash")
                            .documentBinaryUrl("binaryUrl").build())
                    .documentType(DocumentType.DIRECTION_ORDER).documentSize(12L).build();

            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .directionOrderDocument(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getDirectionOrderDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldGenerateDismissalOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().dismissalOrderApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getDismissalOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateDismissalOrderDocument() {
            CaseData caseData = CaseDataBuilder.builder().dismissalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldNotPrintGenerateDismissalOrderDocument_ifApplicantHasBilingualPreference() {
            CaseData caseData = CaseDataBuilder.builder().dismissalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator, times(1))
                    .generate(any(CaseData.class), eq("BEARER_TOKEN"));
            verifyNoMoreInteractions(dismissalOrderGenerator);
            verifyNoInteractions(sendFinalOrderPrintService);

        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().finalOrderFreeForm()
                    .judicialDecision(GAJudicialDecision.builder()
                            .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateFreeFormOrderDocument() {
            CaseData caseData = CaseDataBuilder.builder().finalOrderFreeForm()
                    .judicialDecision(GAJudicialDecision.builder()
                            .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateHearingOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getHearingOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.HEARING_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldCopyHearingOrderDocInTempCollectionIfWelshParty() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .isGaApplicantLip(YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.HEARING_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.HEARING_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateHearingOrderDocumentLip() {
            CaseData caseData = CaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldPrintGenerateWrittenRepresentationSequentialDocumentLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .writtenRepresentationSequentialApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().writtenRepresentationSequentialApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

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

            CaseData caseData = CaseDataBuilder.builder().writtenRepresentationSequentialApplication()
                    .writtenRepSequentialDocument(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator)
                    .generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getWrittenRepSequentialDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateWrittenRepresentationConccurentDocumentLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .writtenRepresentationConcurrentApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verify(writtenRepresentationConcurrentOrderGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateWrittenRepresentationConccurentDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().writtenRepresentationConcurrentApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

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

            CaseData caseData = CaseDataBuilder.builder().writtenRepresentationConcurrentApplication()
                    .writtenRepConcurrentDocument(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator)
                    .generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getWrittenRepConcurrentDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateRequestForInformationDocumentLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .requestForInformationApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            verify(requestForInformationGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));
        }

        @Test
        void shouldGenerateRequestForInformationDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getRequestForInformationDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @Test
        void shouldPrintGenerateSendAppToOtherPartyLip() {
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
                    .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                            .judgeRecitalText("test")
                            .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                            .judgeRequestMoreInfoByDate(now()).build())
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            verify(requestForInformationGenerator, times(2))
                    .generate(any(CaseData.class), any(CaseData.class), eq("BEARER_TOKEN"), any(FlowFlag.class));
            verify(sendFinalOrderPrintService, times(2))
                    .sendJudgeFinalOrderToPrintForLIP(eq("BEARER_TOKEN"), any(Document.class),
                            any(CaseData.class), any(CaseData.class), any(FlowFlag.class));

        }

        @Test
        void shouldGenerateSendAppToOtherParty_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
                    .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                            .judgeRecitalText("test")
                            .requestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                            .judgeRequestMoreInfoByDate(now()).build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

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

            CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
                    .requestForInformationDocument(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator)
                    .generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getRequestForInformationDocument().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(
                value = FinalOrderSelection.class,
                names = {"ASSISTED_ORDER", "FREE_FORM_ORDER"}
        )
        void shouldGenerateGeneralOrderDoc_whenAboutToSubmitEventIsCalled_withFinalOrder(
                FinalOrderSelection selection) {
            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
                    .build()
                    .toBuilder()
                    .finalOrderSelectionGA(selection)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGeneralOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(
                value = FinalOrderSelection.class,
                names = {"ASSISTED_ORDER", "FREE_FORM_ORDER"}
        )
        void shouldHaveListOfTwoGeneralOrderDocumentIfElementInListAlreadyPresent_withFinalOrder(
                FinalOrderSelection selection) {

            CaseDocument caseDocument = CaseDocument.builder().documentName("abcd")
                    .documentLink(Document.builder().documentUrl("url")
                            .documentFileName("filename").documentHash("hash")
                            .documentBinaryUrl("binaryUrl").build())
                    .documentType(DocumentType.GENERAL_ORDER).documentSize(12L).build();

            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
                    .build()
                    .toBuilder()
                    .finalOrderSelectionGA(selection)
                    .generalOrderDocumentGA(wrapElements(caseDocument))
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGeneralOrderDocumentGA().size()).isEqualTo(2);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
        }

        @ParameterizedTest
        @EnumSource(
                value = FinalOrderSelection.class,
                names = {"ASSISTED_ORDER", "FREE_FORM_ORDER"}
        )
        void shouldNotPrintFinalOrderDocument_ifApplicantHasBilingualPreference(
                FinalOrderSelection selection) {
            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
                    .build()
                    .toBuilder()
                    .finalOrderSelection(selection)
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
            verifyNoInteractions(sendFinalOrderPrintService);
        }

        @Test
        void shouldGenerateConsentOrderDocument_whenAboutToSubmitEventIsCalled() {
            CaseData caseData = CaseDataBuilder.builder().consentOrderApplication()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(consentOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getConsentOrderDocument().get(0).getValue())
                    .isEqualTo(PDFBuilder.CONSENT_ORDER_DOCUMENT);

        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslation() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaRespondentOneLip(YesOrNo.YES)
                            .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForWelshTranslation() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaApplicantLip(YesOrNo.YES)
                            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslation() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaRespondentOneLip(YesOrNo.YES)
                            .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForApplicantWelshLip() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(DIRECTIONS_ORDER_DOC);
        }

        @Test
        void shouldPrintGenerateDirectionOrderDocumentForRespondentWelshLip() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(DIRECTIONS_ORDER_DOC);
        }

        @Test
        void shouldNotPauseGenerateDirectionOrderDocumentForLip() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .applicationIsUncloakedOnce(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();

            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            when(gaForLipService.isLipApp(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipResp(any(CaseData.class))).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForWelshTranslation() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaApplicantLip(YesOrNo.YES)
                            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(WRITTEN_REPRESENTATION_ORDER_DOC);
        }

        @Test
        void shouldGenerateWrittenRepresentationConcurrentDocument_whenAboutToSubmitEventIsCalledForNotRespondentWelshTranslation() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationConcurrentApplication().isGaRespondentOneLip(YesOrNo.NO)
                            .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateWrittenRepresentationSequentialDocument_whenAboutToSubmitEventIsCalledForRespondentWelshTranslationWhenFlagOff() {
            CaseData caseData =
                    CaseDataBuilder.builder().writtenRepresentationSequentialApplication().isGaRespondentOneLip(YesOrNo.NO)
                            .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                            .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .generalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForWelshApplicantLip() {
            CaseData caseData = CaseDataBuilder.builder().finalOrderFreeForm()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .judicialDecision(GAJudicialDecision.builder()
                            .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForWelshRespondentLip() {
            CaseData caseData = CaseDataBuilder.builder().finalOrderFreeForm()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .judicialDecision(GAJudicialDecision.builder()
                            .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateMadeDecisionFinalOrderDocument_whenAboutToSubmitEventIsCalledForNonWelshRespondentLip() {
            CaseData caseData = CaseDataBuilder.builder().finalOrderFreeForm()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.NO)
                    .judicialDecision(GAJudicialDecision.builder()
                            .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(freeFormOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForRespondentWelshLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .generalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
                    .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT);
            assertThat(updatedData.getPreTranslationGaDocumentType())
                    .isEqualTo(GENERAL_ORDER_DOC);
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForNonWelshLip() {
            CaseData caseData = CaseDataBuilder.builder()
                    .generalOrderApplication()
                    .isGaApplicantLip(YesOrNo.YES)
                    .isGaRespondentOneLip(YesOrNo.YES)
                    .parentClaimantIsApplicant(YesOrNo.YES)
                    .respondentBilingualLanguagePreferenceGA(YesOrNo.NO)
                    .build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(generalOrderGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLipButDontHideIt() {
            CaseData caseData = CaseDataBuilder.builder()
                    .requestForInformationApplication()
                    .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                            GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY).build())
                    .isGaApplicantLip(YES)
                    .isGaRespondentOneLip(YES)
                    .applicantBilingualLanguagePreferenceGA(YES)
                    .parentClaimantIsApplicant(YES)
                    .build();
            caseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                    .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                    .build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isNull();
            assertThat(updatedData.getRequestForInformationDocument()).isNotEmpty();
        }

        @Test
        void shouldGenerateGeneralOrderDocument_whenAboutToSubmitEventIsCalledForWelshLipButHideIt() {
            CaseData caseData = CaseDataBuilder.builder()
                    .requestForInformationApplication()
                    .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                                    GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).judgeRequestMoreInfoByDate(LocalDate.now().minusDays(1))
                            .judgeRequestMoreInfoText("test").build())
                    .isGaApplicantLip(YES)
                    .isGaRespondentOneLip(YES)
                    .parentClaimantIsApplicant(YES)
                    .applicantBilingualLanguagePreferenceGA(YES)
                    .build();
            caseData = caseData.toBuilder().judicialDecision(GAJudicialDecision.builder()
                    .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO)
                    .build()).build();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(requestForInformationGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getPreTranslationGaDocuments()).isNotEmpty();
            assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.REQUEST_MORE_INFORMATION_ORDER_DOC);
            assertThat(updatedData.getRequestForInformationDocumentGA()).isEmpty();
        }

        @Test
        void shouldReturnCorrectActivityId_whenRequested() {
            CaseData caseData = CaseDataBuilder.builder().generalOrderApplication().build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("CreatePDFDocument");
        }
    }
}
