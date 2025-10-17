package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_WITH_GA_STATE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    EndGeneralAppBusinessProcessCallbackHandler.class,
    CaseDetailsConverter.class,
    CoreCaseDataService.class,
    ParentCaseUpdateHelper.class,
    GaCaseDataEnricher.class,
    ObjectMapper.class,
    JudicialDecisionNotificationUtil.class
})
public class EndGeneralAppBusinessProcessCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EndGeneralAppBusinessProcessCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private GaForLipService gaForLipService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private static final String DUMMY_EMAIL = "test@gmail.com";
    List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

    @Nested
    class AboutToSubmitCallbackGaForLip {

        private ArgumentCaptor<String> parentCaseId = ArgumentCaptor.forClass(String.class);
        private ArgumentCaptor<CaseDataContent> caseDataContent = ArgumentCaptor.forClass(CaseDataContent.class);

        @BeforeEach
        void setUp() {
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
        }

        @Test
        void shouldAddGatoJudgeCollectionFreeApplication() {
            CaseData updatedCaseDate = CaseData.builder()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(NO).build())
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().code("FREE").build()).build())
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                .build();

            GeneralApplicationsDetails judgeCollection = GeneralApplicationsDetails.builder()
                .build();
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();
            GADetailsRespondentSol respondentOneCollection = GADetailsRespondentSol.builder().build();

            CaseData parentCaseData = CaseData.builder()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseData(NO).getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(getCallbackParamsGaForLipCaseData(NO));
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().get(0).getData(),
                              new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughServiceRequestAndHwfIsNull() {
            CaseData updatedCaseDate = CaseData.builder()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().code("PAY").build()).build())
                .build();

            GeneralApplicationsDetails judgeCollection = GeneralApplicationsDetails.builder()
                .build();
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();
            GADetailsRespondentSol respondentOneCollection = GADetailsRespondentSol.builder().build();

            CaseData parentCaseData = CaseData.builder()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseData(NO).getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(getCallbackParamsGaForLipCaseData(NO));
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().get(0).getData(),
                              new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughServiceRequest() {
            CaseData updatedCaseDate = CaseData.builder()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(NO).build())
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().code("PAY").build()).build())
                .build();

            GeneralApplicationsDetails judgeCollection = GeneralApplicationsDetails.builder()
                .build();
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();
            GADetailsRespondentSol respondentOneCollection = GADetailsRespondentSol.builder().build();

            CaseData parentCaseData = CaseData.builder()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseData(NO).getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(getCallbackParamsGaForLipCaseData(NO));
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().get(0).getData(),
                              new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughHelpWithFeesFullRemission() {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            CaseData updatedCaseDate = CaseData.builder()
                    .parentClaimantIsApplicant(YES)
                    .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YES).build())
                    .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                            .hwfFullRemissionGrantedForGa(YES).build())
                    .isMultiParty(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                    .ccdState(AWAITING_APPLICATION_PAYMENT)
                    .ccdCaseReference(1234L)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                    .generalAppType(GAApplicationType.builder().types(types).build())
                    .generalAppPBADetails(GAPbaDetails.builder()
                            .fee(Fee.builder().code("PAY").build())
                            .paymentDetails(PaymentDetails.builder().build())
                            .build())
                    .build();

            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                    .caseState("Awaiting Application Payment")
                    .caseLink(CaseLink.builder()
                            .caseReference("1234")
                            .build())
                    .build();

            CaseData parentCaseData = CaseData.builder()
                    .claimantGaAppDetails(wrapElements(claimantCollection))
                    .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseDataFullRemission().getRequest().getCaseDetails()))
                    .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(getCallbackParamsGaForLipCaseDataFullRemission());
            verify(coreCaseDataService, times(2))
                    .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                    .convertValue(caseDataContent.getAllValues().get(0).getData(),
                            new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                            .get("gaDetailsMasterCollection"),
                    new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughHelpWithFeesPartRemission() {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            CaseData updatedCaseDate = CaseData.builder()
                    .parentClaimantIsApplicant(YES)
                    .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YES).build())
                    .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                            .hwfFullRemissionGrantedForGa(NO)
                            .hwfOutstandingFeePaymentDoneForGa(List.of("Yes")).build())
                    .isMultiParty(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                    .ccdState(AWAITING_APPLICATION_PAYMENT)
                    .ccdCaseReference(1234L)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                    .generalAppType(GAApplicationType.builder().types(types).build())
                    .generalAppPBADetails(GAPbaDetails.builder()
                            .fee(Fee.builder().code("PAY").build())
                            .paymentDetails(PaymentDetails.builder().build())
                            .build())
                    .build();

            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                    .caseState("Awaiting Application Payment")
                    .caseLink(CaseLink.builder()
                            .caseReference("1234")
                            .build())
                    .build();

            CaseData parentCaseData = CaseData.builder()
                    .claimantGaAppDetails(wrapElements(claimantCollection))
                    .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseDataPartRemission().getRequest().getCaseDetails()))
                    .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(getCallbackParamsGaForLipCaseDataPartRemission());
            verify(coreCaseDataService, times(2))
                    .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                    .convertValue(caseDataContent.getAllValues().get(0).getData(),
                            new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                            .get("gaDetailsMasterCollection"),
                    new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGatoJudgeCollectionForCaseWorker() {
            CaseData updatedCaseDate = CaseData.builder()
                .isGaApplicantLip(YES)
                .isGaRespondentTwoLip(YES)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YES).build())
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppPBADetails(GAPbaDetails.builder()
                                          .fee(Fee.builder().code("PAY").build()).build())
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("0000").build())
                .build();

            GeneralApplicationsDetails judgeCollection = GeneralApplicationsDetails.builder()
                .build();
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();
            GADetailsRespondentSol respondentOneCollection = GADetailsRespondentSol.builder().build();

            CaseData parentCaseData = CaseData.builder()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsGaForLipCaseData(NO).getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse().getCaseDetails())).thenReturn(parentCaseData);
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            handler.handle(getCallbackParamsGaForLipCaseData(NO));
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            verify(coreCaseDataService, times(2))
                .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().get(0).getData(),
                              new TypeReference<Map<String, Object>>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);

        }

        public CallbackParams getCallbackParamsGaForLipCaseData(YesOrNo hwf) {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            CaseData caseData = CaseData.builder()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(hwf).build())
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {}))
                .build();

            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData)
                .build();
        }

        public CallbackParams getCallbackParamsGaForLipCaseDataFullRemission() {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            CaseData caseData = CaseData.builder()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YES).build())
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                              .hwfFullRemissionGrantedForGa(YES).build())
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {}))
                .build();

            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData)
                .build();
        }

        public CallbackParams getCallbackParamsGaForLipCaseDataPartRemission() {
            List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
            CaseData caseData = CaseData.builder()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YES).build())
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                              .hwfFullRemissionGrantedForGa(NO)
                                              .hwfOutstandingFeePaymentDoneForGa(List.of("Yes"))
                                              .build())
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {}))
                .build();

            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData)
                .build();
        }

        public StartEventResponse getStartEventResponse() {
            GeneralApplicationsDetails judgeCollection = GeneralApplicationsDetails.builder()
                .build();
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234L")
                              .build())
                .build();
            GADetailsRespondentSol respondentOneCollection = GADetailsRespondentSol.builder().build();

            CaseData caseData = CaseData.builder()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();
            CaseDetails caseDetails = CaseDetails.builder().data(objectMapper.convertValue(
                caseData,
                new TypeReference<Map<String, Object>>() {})).build();

            return StartEventResponse.builder().caseDetails(caseDetails).build();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private final ArgumentCaptor<String> parentCaseId = ArgumentCaptor.forClass(String.class);
        private final ArgumentCaptor<CaseDataContent> caseDataContent = ArgumentCaptor.forClass(CaseDataContent.class);

        @Test
        void shouldChangeStateToRespondentResponseWhenVaryJudgmentWhenParentIsNotClaimantAndNoResponse() {
            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();
            respondentsResponses.add(element(respondent1Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsOfVary(YES, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForVaryJudgement(YES, YES, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParamsOfVary(YES, YES));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                                                                    new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);

            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Respondent Response");;

        }

        @Test
        void shouldChangeStateToProceedsInHeritageWhenVaryJudgmentWhenParentIsNotClaimant() {

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();
            respondentsResponses.add(element(respondent1Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsOfVary(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForVaryJudgement(NO, YES, respondentsResponses).toBuilder().respondentsResponses(respondentsResponses).build());
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            handler.handle(getCallbackParamsOfVary(NO, YES));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                                                                    new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);

            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Proceeds In Heritage");;

        }

        @Test
        void shouldReturn_Awaiting_respondent_response_3Def_1Response_Vary() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent3 = GASolicitorDetailsGAspec.builder().id("id3")
                .email(DUMMY_EMAIL).organisationIdentifier("org3").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();
            GARespondentResponse respondent2Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id3")
                .build();
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols));
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name());
        }

        @Test
        void shouldChangeStateToApplicationDismissedWhenCOSC() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForCCJ(YES, NO));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                                                                    new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);

            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Application Dismissed");

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Application Dismissed");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Application Dismissed");

        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetails_NotToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseData(YES, NO));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                    .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(1))
                    .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                    new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                    updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);

            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Application Submitted - Awaiting Judicial Decision");
        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForDirectionOrder() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_DIRECTIONS_ORDER_DOCS));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForWrittenRep() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_WRITTEN_REPRESENTATIONS));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForAddlInfo() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_ADDITIONAL_INFORMATION));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForRespondResponseState() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_RESPONDENT_RESPONSE));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetails_ToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseData(NO, YES));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                    .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            handler.handle(getCallbackParams(NO, YES));

            verify(coreCaseDataService, times(1))
                    .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                    new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                    updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Awaiting Respondent Response");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Awaiting Respondent Response");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Awaiting Respondent Response");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Respondent Response");
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetailsAndCollection_ToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponseForCollection(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForCollection(NO, YES));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponseForCollection(NO, YES).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdateCollection(NO, YES));

            handler.handle(getCallbackParams(NO, YES));

            verify(coreCaseDataService, times(2))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(2))
                .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
            verify(coreCaseDataService, times(2)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());

            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();
            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                                                                    new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
        }

        @Test
        void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_1Def_1Response() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

            respondentSols.add(element(respondent1));

            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getCase(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            var response = handler.handle(getCallbackParams(NO, YES));
            assertThat(response).isNotNull();
        }

        @Test
        void shouldReturn_Awaiting_respondent_response_3Def_1Response() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent3 = GASolicitorDetailsGAspec.builder().id("id3")
                .email(DUMMY_EMAIL).organisationIdentifier("org3").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();
            GARespondentResponse respondent2Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id3")
                .build();
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols));
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name());
        }

        @Test
        void shouldNotReturn_Application_Submitted_Awaiting_Judicial_Decision_3Def_1Response() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                    .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                    .generalAppRespondent1Representative(YES)
                    .gaRespondentDetails("id")
                    .build();
            GARespondentResponse respondent2Response = GARespondentResponse.builder()
                    .generalAppRespondent1Representative(YES)
                    .gaRespondentDetails("id2")
                    .build();
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                    .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, NO).getCaseDetails()))
                    .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols));
            assertThat(response.getState()).isEqualTo(AWAITING_RESPONDENT_RESPONSE.name());
        }

        @Test
        void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_3Def_2Response() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent3 = GASolicitorDetailsGAspec.builder().id("id3")
                .email(DUMMY_EMAIL).organisationIdentifier("org3").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));
            respondentSols.add(element(respondent3));

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();
            GARespondentResponse respondent2Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id3")
                .build();
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            var response = handler.handle(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols));
            assertThat(response).isNotNull();
        }

        @Test
        void shouldReturn_Application_Submitted_Awaiting_Judicial_Decision_2Def_1Response() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id2")
                .email(DUMMY_EMAIL).organisationIdentifier("org2").build();
            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = GARespondentResponse.builder()
                .generalAppRespondent1Representative(YES)
                .gaRespondentDetails("id")
                .build();

            respondentsResponses.add(element(respondent1Response));

            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, NO).getRequest().getCaseDetails()))
                .thenReturn(getCase(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            var response = handler.handle(getCallbackParams(NO, NO));
            assertThat(response).isNotNull();
        }

        @Test
        void shouldChangeTheStateToAwaitingApplicationPaymentBeforePayment() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataBeforePayment(NO, YES));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            handler.handle(getCallbackParams(NO, YES));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                                                                    new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                                                                              .get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Awaiting Application Payment");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Awaiting Application Payment");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Awaiting Application Payment");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Application Payment");
        }

        @ParameterizedTest
        @EnumSource(value = FinalOrderSelection.class)
        void shouldChangeTheStateToOrderMadeAfterFinalOrder(FinalOrderSelection selection) {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseDataAfterOrderMade(NO,
                            YES, selection, null));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                    .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            handler.handle(getCallbackParams(NO, YES));

            verify(coreCaseDataService, times(1))
                    .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

            verify(coreCaseDataService).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                    new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                    updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                    updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                    updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                            .get("gaDetailsMasterCollection"),
                    new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Order Made");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Order Made");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                    .isEqualTo("Order Made");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                    .isEqualTo("Order Made");
        }

        @Test
        void shouldChangeTheStateToListingForAHearingAfterFinalOrder() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toCaseDataGA(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseDataAfterOrderMade(NO,
                            YES, FinalOrderSelection.ASSISTED_ORDER,
                            AssistedOrderFurtherHearingDetails.builder().build()));
            when(caseDetailsConverter.toCaseDataGA(getStartEventResponse(NO, YES).getCaseDetails()))
                    .thenReturn(getParentCaseDataBeforeUpdate(NO, YES));

            handler.handle(getCallbackParams(NO, YES));

            verify(coreCaseDataService, times(1)).submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            HashMap<?, ?> updatedCaseData = (HashMap<?, ?>) caseDataContent.getValue().getData();

            List<?> generalApplications = objectMapper.convertValue(updatedCaseData.get("generalApplications"),
                    new TypeReference<>(){});
            List<?> generalApplicationDetails = objectMapper.convertValue(
                    updatedCaseData.get("claimantGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSol = objectMapper.convertValue(
                    updatedCaseData.get("respondentSolGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsRespondentSolTwo = objectMapper.convertValue(
                    updatedCaseData.get("respondentSolTwoGaAppDetails"), new TypeReference<>(){});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(updatedCaseData
                            .get("gaDetailsMasterCollection"),
                    new TypeReference<>(){});

            assertThat(generalApplications.size()).isEqualTo(1);
            assertThat(generalApplicationDetails.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSol.size()).isEqualTo(1);
            assertThat(gaDetailsRespondentSolTwo.size()).isEqualTo(1);
            assertThat(gaDetailsMasterCollection.size()).isEqualTo(1);

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Listed for a Hearing");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Listed for a Hearing");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                    .isEqualTo("Listed for a Hearing");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.get(0)).get("value"),
                    new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                    .isEqualTo("Listed for a Hearing");
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(END_BUSINESS_PROCESS_GASPEC);
        }

        private GeneralApplication getGeneralApplication(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplication.builder()
                .caseLink(CaseLink.builder().caseReference("1646003133062762").build())
                    .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
                .generalAppPBADetails(
                    GAPbaDetails.builder()
                        .paymentDetails(PaymentDetails.builder()
                                            .status(PaymentStatus.SUCCESS)
                                            .reference("RC-1658-4258-2679-9795")
                                            .customerReference(CUSTOMER_REFERENCE)
                                            .build())
                        .fee(
                            Fee.builder()
                                .code("FE203")
                                .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                .version("1")
                                .build())
                        .serviceReqReference(CUSTOMER_REFERENCE).build())
                    .generalAppDetailsOfOrder(STRING_CONSTANT)
                    .generalAppReasonsOfOrder(STRING_CONSTANT)
                    .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                    .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
                    .generalAppHearingDetails(GAHearingDetails.builder().build())
                    .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                            .email("abc@gmail.com").build()))
                    .isMultiParty(NO)
                    .parentClaimantIsApplicant(isConsented)
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                            .caseReference(PARENT_CCD_REF.toString()).build())
                    .build();
        }

        private GeneralApplication getGeneralApplicationVary(YesOrNo isConsented, YesOrNo isTobeNotified, List<Element<GARespondentResponse>> respondentsResponses) {

            return GeneralApplication.builder()
                .caseLink(CaseLink.builder().caseReference("1646003133062762").build())
                .generalAppType(GAApplicationType.builder().types(List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
                .generalAppPBADetails(
                    GAPbaDetails.builder()
                        .paymentDetails(PaymentDetails.builder()
                                            .status(PaymentStatus.SUCCESS)
                                            .reference("RC-1658-4258-2679-9795")
                                            .customerReference(CUSTOMER_REFERENCE)
                                            .build())
                        .fee(
                            Fee.builder()
                                .code("FE203")
                                .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                .version("1")
                                .build())
                        .serviceReqReference(CUSTOMER_REFERENCE).build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
                .generalAppHearingDetails(GAHearingDetails.builder().build())
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                                 .email("abc@gmail.com").build()))
                .respondentsResponses(respondentsResponses)
                .isMultiParty(NO)
                .parentClaimantIsApplicant(isConsented)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(PARENT_CCD_REF.toString()).build())
                .build();
        }

        private GeneralApplication getGeneralApplicationMulti(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                              List<Element<GARespondentResponse>> respondentResponses,
                                                              List<Element<GASolicitorDetailsGAspec>> respondentDetails) {
            return GeneralApplication.builder()
                .caseLink(CaseLink.builder().caseReference("1646003133062762").build())
                .generalAppType(GAApplicationType.builder().types(List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
                .generalAppPBADetails(
                    GAPbaDetails.builder()
                        .paymentDetails(PaymentDetails.builder()
                                            .status(PaymentStatus.SUCCESS)
                                            .reference("RC-1658-4258-2679-9795")
                                            .customerReference(CUSTOMER_REFERENCE)
                                            .build())
                        .fee(
                            Fee.builder()
                                .code("FE203")
                                .calculatedAmountInPence(BigDecimal.valueOf(27500))
                                .version("1")
                                .build())
                        .serviceReqReference(CUSTOMER_REFERENCE).build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
                .generalAppHearingDetails(GAHearingDetails.builder().build())
                .isMultiParty(isConsented)
                .respondentsResponses(respondentResponses)
                .generalAppRespondentSolicitors(respondentDetails)
                .parentClaimantIsApplicant(isConsented)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(PARENT_CCD_REF.toString()).build())
                .build();
        }

        private GeneralApplication getGeneralApplicationBeforePayment(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplication.builder()
                .caseLink(CaseLink.builder().caseReference("1646003133062762L").build())
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
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
                .generalAppHearingDetails(GAHearingDetails.builder().build())
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                                 .email("abc@gmail.com").build()))
                .isMultiParty(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(PARENT_CCD_REF.toString()).build())
                .build();
        }

        private CaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication(isConsented, isTobeNotified))
                    .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataForCCJ(YesOrNo isConsented, YesOrNo isTobeNotified) {
            List<GeneralApplicationTypes> types = Arrays.asList(CONFIRM_CCJ_DEBT_PAID);
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication(isConsented, isTobeNotified))
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).generalAppType(GAApplicationType.builder().types(types).build()).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataForVaryJudgement(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                                             List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = Arrays.asList(VARY_PAYMENT_TERMS_OF_JUDGMENT);

            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationVary(isConsented, isTobeNotified, respondentsResponses))
                .toBuilder().ccdCaseReference(CHILD_CCD_REF)
                .generalAppType(GAApplicationType.builder().types(types).build()).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataMulti(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                                  List<Element<GARespondentResponse>> respondentResponses,
                                                                  List<Element<GASolicitorDetailsGAspec>> respondentDetails) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationMulti(isConsented, isTobeNotified, respondentResponses, respondentDetails))
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataByState(YesOrNo isConsented, YesOrNo isTobeNotified, CaseState caseState) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplicationByState(
                    getGeneralApplication(isConsented, isTobeNotified), caseState)
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataForCollection(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGaForCollection(
                    getGeneralApplication(isConsented, isTobeNotified))
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataBeforePayment(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationBeforePayment(isConsented, isTobeNotified))
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private CaseData getSampleGeneralApplicationCaseDataAfterOrderMade(
                YesOrNo isConsented,
                YesOrNo isTobeNotified,
                FinalOrderSelection selection,
                AssistedOrderFurtherHearingDetails hearingDetails) {
            return CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                            getGeneralApplication(isConsented, isTobeNotified))
                    .toBuilder().ccdCaseReference(CHILD_CCD_REF)
                    .finalOrderSelection(selection).assistedOrderFurtherHearingDetails(hearingDetails).build();
        }

        private CallbackParams getCallbackParamsMulti(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                      List<Element<GARespondentResponse>> respondentResponses,
                                                      List<Element<GASolicitorDetailsGAspec>> respondentDetails) {
            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .pageId(null)
                .request(CallbackRequest.builder()
                             .caseDetails(CaseDetails.builder()
                                              .data(objectMapper.convertValue(
                                                  getSampleGeneralApplicationCaseDataMulti(isConsented, isTobeNotified,
                                                                                           respondentResponses, respondentDetails),
                                                  new TypeReference<Map<String, Object>>() {})).id(CASE_ID).build())
                             .eventId("END_BUSINESS_PROCESS_GASPEC")
                             .build())
                .caseData(getSampleGeneralApplicationCaseDataMulti(isConsented, isTobeNotified,
                                                                   respondentResponses, respondentDetails))
                .version(null)
                .params(null)
                .build();
        }

        private CallbackParams getCallbackParamsOfVary(YesOrNo isConsented, YesOrNo isTobeNotified) {
            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
            return CallbackParams.builder()
                .type(ABOUT_TO_SUBMIT)
                .pageId(null)
                .request(CallbackRequest.builder()
                             .caseDetails(CaseDetails.builder()
                                              .data(objectMapper.convertValue(
                                                  getSampleGeneralApplicationCaseDataForVaryJudgement(isConsented, isTobeNotified, respondentsResponses),
                                                  new TypeReference<Map<String, Object>>() {})).id(CASE_ID).build())
                             .eventId("END_BUSINESS_PROCESS_GASPEC")
                             .build())
                .caseData(getSampleGeneralApplicationCaseDataForVaryJudgement(isConsented, isTobeNotified, respondentsResponses))
                .version(null)
                .params(null)
                .build();
        }

        private CallbackParams getCallbackParams(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CallbackParams.builder()
                    .type(ABOUT_TO_SUBMIT)
                    .pageId(null)
                    .request(CallbackRequest.builder()
                            .caseDetails(CaseDetails.builder()
                                    .data(objectMapper.convertValue(
                                            getSampleGeneralApplicationCaseData(isConsented, isTobeNotified),
                                            new TypeReference<Map<String, Object>>() {})).id(CASE_ID).build())
                            .eventId("END_BUSINESS_PROCESS_GASPEC")
                            .build())
                    .caseData(getSampleGeneralApplicationCaseData(isConsented, isTobeNotified))
                    .version(null)
                    .params(null)
                    .build();
        }

        private StartEventResponse getStartEventResponse(YesOrNo isConsented, YesOrNo isTobeNotified) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(
                    getParentCaseDataBeforeUpdate(isConsented, isTobeNotified))
                    .id(1645779506193000L)
                    .state(PENDING_CASE_ISSUED)
                    .build();
            StartEventResponse.StartEventResponseBuilder startEventResponseBuilder = StartEventResponse.builder();
            startEventResponseBuilder.eventId(UPDATE_CASE_WITH_GA_STATE.toString())
                    .token("BEARER_TOKEN")
                    .caseDetails(caseDetails);

            return startEventResponseBuilder.build();
        }

        private StartEventResponse getStartEventResponseForCollection(YesOrNo isConsented, YesOrNo isTobeNotified) {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(
                    getParentCaseDataBeforeUpdate(isConsented, isTobeNotified))
                .id(1645779506193000L)
                .state(AWAITING_APPLICATION_PAYMENT)
                .build();
            StartEventResponse.StartEventResponseBuilder startEventResponseBuilder = StartEventResponse.builder();
            startEventResponseBuilder.eventId(UPDATE_CASE_WITH_GA_STATE.toString())
                .token("BEARER_TOKEN")
                .caseDetails(caseDetails);

            return startEventResponseBuilder.build();
        }

        private CaseData getParentCaseDataBeforeUpdate(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CaseData.builder()
                    .generalApplications(wrapElements(getGeneralApplication(isConsented, isTobeNotified)))
                    .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                            .caseLink(CaseLink.builder().caseReference(CHILD_CCD_REF.toString()).build())
                            .caseState("General Application Issue Pending")
                            .build()))
                    .gaDetailsMasterCollection(wrapElements(GeneralApplicationsDetails.builder()
                                                                .caseLink(CaseLink.builder()
                                                                              .caseReference(CHILD_CCD_REF.toString())
                                                                              .build())
                                                                .caseState("General Application Issue Pending")
                                                                .build()))
                    .respondentSolGaAppDetails(wrapElements(GADetailsRespondentSol.builder()
                             .caseLink(CaseLink.builder().caseReference(CHILD_CCD_REF.toString()).build())
                             .caseState("General Application Issue Pending")
                             .build()))
                    .respondentSolTwoGaAppDetails(wrapElements(GADetailsRespondentSol.builder()
                              .caseLink(CaseLink.builder().caseReference(CHILD_CCD_REF.toString()).build())
                              .caseState("General Application Issue Pending")
                              .build()))
                    .build();
        }

        private CaseData getParentCaseDataBeforeUpdateCollection(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return CaseData.builder()
                .generalApplications(wrapElements(getGeneralApplication(isConsented, isTobeNotified)))
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseLink(CaseLink.builder().caseReference(CHILD_CCD_REF.toString()).build())
                                                       .caseState("Awaiting Application Payment")
                                                       .build()))
                .build();
        }

        private CaseData getCase(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                                 List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            DynamicList dynamicListTest = fromList(getSampleCourLocations());
            Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
            first.ifPresent(dynamicListTest::setValue);

            return CaseData.builder()
                .ccdCaseReference(CHILD_CCD_REF)
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(PARENT_CCD_REF.toString()).build())
                .generalAppPBADetails(GAPbaDetails.builder().paymentDetails(PaymentDetails.builder()
                                                                                .customerReference("1336546")
                                                                                .build()).build())
                .generalAppRespondentSolicitors(respondentSols)
                .hearingDetailsResp(GAHearingDetails.builder()
                                        .hearingPreferredLocation(
                                            dynamicListTest)
                                        .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                        .build())
                .respondentsResponses(respondentsResponses)
                .isMultiParty(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppRespondent1Representative(
                    GARespondentRepresentative.builder()
                        .generalAppRespondent1Representative(YES)
                        .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .build();
        }

        private CaseData getCaseMulti(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                                 List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = List.of(
                (RELIEF_FROM_SANCTIONS));
            DynamicList dynamicListTest = fromList(getSampleCourLocations());
            Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
            first.ifPresent(dynamicListTest::setValue);

            return CaseData.builder()
                .ccdCaseReference(CHILD_CCD_REF)
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(PARENT_CCD_REF.toString()).build())
                .generalAppPBADetails(GAPbaDetails.builder().paymentDetails(PaymentDetails.builder()
                                                                                .customerReference("1336546")
                                                                                .build()).build())
                .generalAppRespondentSolicitors(respondentSols)
                .isMultiParty(YES)
                .parentClaimantIsApplicant(NO)
                .hearingDetailsResp(GAHearingDetails.builder()
                                        .hearingPreferredLocation(
                                            dynamicListTest)
                                        .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                        .build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .respondentsResponses(respondentsResponses)
                .generalAppRespondent1Representative(
                    GARespondentRepresentative.builder()
                        .generalAppRespondent1Representative(YES)
                        .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .build();
        }

        protected List<String> getSampleCourLocations() {
            return new ArrayList<>(Arrays.asList("ABCD - RG0 0AL", "PQRS - GU0 0EE", "WXYZ - EW0 0HE", "LMNO - NE0 0BH"));
        }
    }

}
