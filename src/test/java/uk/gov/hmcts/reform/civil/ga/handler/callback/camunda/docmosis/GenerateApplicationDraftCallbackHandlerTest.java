package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.ga.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralApplicationDraftGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateApplicationDraftCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
class GenerateApplicationDraftCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @MockBean
    private Time time;
    @MockBean
    private GeneralApplicationDraftGenerator generalApplicationDraftGenerator;
    @Autowired
    private GenerateApplicationDraftCallbackHandler handler;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private AssignCategoryId assignCategoryId;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private GeneralAppFeesService generalAppFeesService;
    @MockBean
    private GaForLipService gaForLipService;

    private final LocalDate submittedOn = now();
    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String DUMMY_TELEPHONE_NUM = "234345435435";
    public static final LocalDate APPLICATION_SUBMITTED_DATE = now();
    private static final String TASK_ID = "GenerateDraftDocumentId";

    @Test
    void shouldTriggerTheEventAndAboutToSubmit() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES, NO);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo(TASK_ID);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            GENERATE_DRAFT_DOCUMENT);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithoutNotice_LR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldNotGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithNoticeAndNotUrgent_LR() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, YES, NO);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);

        verifyNoInteractions(generalApplicationDraftGenerator);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithNoticeAndUrgent_LR() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, YES, YES);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithoutNoticeAndNonUrgent_LR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, NO);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldSetTranslationDocumentsForWlu_Lip() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, YES, YES);
        caseData = caseData.toBuilder()
            .applicantBilingualLanguagePreference(YES)
            .isGaApplicantLip(YES)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC);
    }

    @Test
    void shouldSetTranslationDocumentsForWlu_LipRespondent() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, YES, YES);
        caseData = caseData.toBuilder()
            .respondentBilingualLanguagePreference(YES)
            .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.APPLICATION_SUMMARY_DOC);
    }

    @Test
    void shouldSetTranslationDocumentsForWlu_LipWhenRespondentRespond() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataWithResponse(YES, YES, YES);
        caseData = caseData.toBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
            .respondentBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(PreTranslationGaDocumentType.RESPOND_TO_APPLICATION_SUMMARY_DOC);
    }

    @Test
    void shouldSetTranslationDocumentsForWlu_LipWhenRespondentResponseNull() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, YES, YES);
        caseData = caseData.toBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
            .respondentBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getPreTranslationGaDocuments().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
    }

    @Test
    void shouldGenerateDraftDocument_FreeFeeCode_ConsentApp_LR() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, YES);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("FREE").build()).build()).build();
        when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(true);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldNotGenerateDraftDocument_Unpaid_ConsentApp_LR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, NO, NO);
        when(generalAppFeesService.isFreeApplication(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        verifyNoInteractions(generalApplicationDraftGenerator);
    }

    @Test
    void shouldNotGenerateDraftDocument_whenFeeUnpaid_WithNoticeAndUrgent_LR() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, YES, YES);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        verifyNoInteractions(generalApplicationDraftGenerator);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithNoticeAndUrgent_Lip() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, YES, YES);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndWithNoticeAndUrgentbefore_Lip() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(YES, YES, YES);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        caseData = caseData.toBuilder()
            .gaDraftDocument(null)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("NotFree").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndFreeApp_Lip() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, NO, NO);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .paymentDetails(PaymentDetails.builder()
                                                          .status(PaymentStatus.SUCCESS).build())
                                      .fee(Fee.builder().code("Free").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(generalApplicationDraftGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData.getGaDraftDocument().get(0).getValue())
            .isEqualTo(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        assertThat(updatedData.getSubmittedOn()).isEqualTo(submittedOn);
    }

    @Test
    void shouldNotGenerateApplicationDraftDocument_whenAboutToSubmitEventIsCalledAndFreeApp_Lip() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseDataLip(YES, NO, NO);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(generalAppFeesService.isFreeApplication(any())).thenReturn(false);
        caseData = caseData.toBuilder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder().code("Free").build()).build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(generalApplicationDraftGenerator.generate(any(GeneralApplicationCaseData.class), anyString()))
            .thenReturn(PDFBuilder.APPLICATION_DRAFT_DOCUMENT);
        when(time.now()).thenReturn(submittedOn.atStartOfDay());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verifyNoInteractions(generalApplicationDraftGenerator);
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified, YesOrNo isUrgent) {
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication(isConsented, isTobeNotified, isUrgent))
            .toBuilder()
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF)
            .submittedOn(APPLICATION_SUBMITTED_DATE).build();
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataLip(YesOrNo isConsented, YesOrNo isTobeNotified, YesOrNo isUrgent) {
        List<Element<CaseDocument>> gaDraft = new ArrayList<>();
        gaDraft.addAll(wrapElements(PDFBuilder.APPLICATION_DRAFT_DOCUMENT));
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication(isConsented, isTobeNotified, isUrgent))
            .toBuilder()
            .gaDraftDocument(gaDraft)
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF)
            .submittedOn(APPLICATION_SUBMITTED_DATE).build();
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataWithResponse(YesOrNo isConsented, YesOrNo isTobeNotified, YesOrNo isUrgent) {
        List<Element<CaseDocument>> gaDraft = new ArrayList<>();
        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

        GARespondentResponse respondent1Response = GARespondentResponse.builder()
            .generalAppRespondent1Representative(YES)
            .gaRespondentDetails("id")
            .build();
        respondentsResponses.add(element(respondent1Response));
        gaDraft.addAll(wrapElements(PDFBuilder.APPLICATION_DRAFT_DOCUMENT));
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication(isConsented, isTobeNotified, isUrgent))
            .toBuilder()
            .gaDraftDocument(gaDraft)
            .respondentsResponses(respondentsResponses)
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF)
            .submittedOn(APPLICATION_SUBMITTED_DATE).build();
    }

    private GeneralApplication getGeneralApplication(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                     YesOrNo isUrgent) {
        DynamicListElement location1 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(isUrgent).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferredLocation(DynamicList.builder()
                                                                        .listItems(List.of(location1))
                                                                        .value(location1).build())
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy2")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.MINUTES_30)
                                          .hearingDetailsEmailID(DUMMY_EMAIL)
                                          .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM).build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                             .email("abc@gmail.com").build()))
            .isMultiParty(NO)
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(PARENT_CCD_REF.toString()).build())
            .build();
    }

}
