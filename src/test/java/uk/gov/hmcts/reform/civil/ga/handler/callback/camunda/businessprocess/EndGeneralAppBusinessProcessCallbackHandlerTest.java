package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;

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
import static org.mockito.Mockito.spy;
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

@ExtendWith(MockitoExtension.class)
public class EndGeneralAppBusinessProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private EndGeneralAppBusinessProcessCallbackHandler handler;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private GaForLipService gaForLipService;

    @Mock
    private FeatureToggleService featureToggleService;

    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @BeforeEach
    void setUpHandler() {
        parentCaseUpdateHelper = spy(new ParentCaseUpdateHelper(
            caseDetailsConverter,
            coreCaseDataService,
            featureToggleService,
            objectMapper
        ));
        handler = new EndGeneralAppBusinessProcessCallbackHandler(
            caseDetailsConverter,
            gaForLipService,
            parentCaseUpdateHelper
        );
    }

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
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
        }

        @Test
        void shouldAddGatoJudgeCollectionFreeApplication() {
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(NO))
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .build();

            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();
            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseData(NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughServiceRequestAndHwfIsNull() {
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("PAY")))
                .build();

            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();

            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseData(NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughServiceRequest() {
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .isGaRespondentOneLip(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(NO))
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("PAY")))
                .build();

            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234")
                              .build())
                .build();

            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseData(NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughHelpWithFeesFullRemission() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT);
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(YES))
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForGa(YES))
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .generalAppType(GAApplicationType.builder().types(types).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAY"))
                                          .setPaymentDetails(new PaymentDetails())
                                          )
                .build();

            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseState("Awaiting Application Payment")
                                                       .caseLink(CaseLink.builder()
                                                                     .caseReference("1234")
                                                                     .build())
                                                       .build()))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseDataFullRemission();
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGaToJudgeCollectionPaymentThroughHelpWithFeesPartRemission() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT);
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(YES))
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForGa(NO)
                                              .setHwfOutstandingFeePaymentDoneForGa(List.of("Yes")))
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .generalAppType(GAApplicationType.builder().types(types).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAY"))
                                          .setPaymentDetails(new PaymentDetails())
                                          )
                .build();

            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseState("Awaiting Application Payment")
                                                       .caseLink(CaseLink.builder()
                                                                     .caseReference("1234")
                                                                     .build())
                                                       .build()))
                .build();

            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseDataPartRemission();
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        @Test
        void shouldAddGatoJudgeCollectionForCaseWorker() {
            GeneralApplicationCaseData updatedCaseDate = new GeneralApplicationCaseData()
                .isGaApplicantLip(YES)
                .isGaRespondentTwoLip(YES)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(YES)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(YES))
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAY")))
                .ccdCaseReference(1234L)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("0000"))
                .build();

            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseState("Awaiting Application Payment")
                                                       .caseLink(CaseLink.builder()
                                                                     .caseReference("1234")
                                                                     .build())
                                                       .build()))
                .build();

            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            CallbackParams callbackParams = getCallbackParamsGaForLipCaseData(NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails()))
                .thenReturn(updatedCaseDate);
            StartEventResponse startEventResponse = getStartEventResponse();
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(startEventResponse);
            when(caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails())).thenReturn(parentCaseData);
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            handler.handle(callbackParams);
            verify(coreCaseDataService, times(2))
                .submitUpdate(parentCaseId.capture(), caseDataContent.capture());
            verify(coreCaseDataService, times(2))
                .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
            assertThat(caseDataContent.getAllValues()).hasSize(2);

            Map<String, Object> map = objectMapper
                .convertValue(caseDataContent.getAllValues().getFirst().getData(), new TypeReference<>() {});
            List<?> gaDetailsMasterCollection = objectMapper.convertValue(map.get("gaDetailsMasterCollection"),
                                                                          new TypeReference<>(){});
            assertThat(gaDetailsMasterCollection).hasSize(1);
        }

        public CallbackParams getCallbackParamsGaForLipCaseData(YesOrNo hwf) {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(hwf))
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<>() {
                }))
                .build();

            return new CallbackParams()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData);
        }

        public CallbackParams getCallbackParamsGaForLipCaseDataFullRemission() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(YES))
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForGa(YES))
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<>() {
                }))
                .build();

            return new CallbackParams()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData);
        }

        public CallbackParams getCallbackParamsGaForLipCaseDataPartRemission() {
            List<GeneralApplicationTypes> types = List.of(STRIKE_OUT);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isGaApplicantLip(YES)
                .generalAppType(GAApplicationType.builder().types(types).build())
                .ccdState(AWAITING_APPLICATION_PAYMENT)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFee(YES))
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                              .setHwfFullRemissionGrantedForGa(NO)
                                              .setHwfOutstandingFeePaymentDoneForGa(List.of("Yes")))
                .build();

            CaseDetails caseDetails = CaseDetails
                .builder()
                .data(objectMapper.convertValue(caseData, new TypeReference<>() {
                }))
                .build();

            return new CallbackParams()
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder().caseDetails(caseDetails)
                             .build())
                .caseData(caseData);
        }

        public StartEventResponse getStartEventResponse() {
            GeneralApplicationsDetails claimantCollection = GeneralApplicationsDetails.builder()
                .caseState("Awaiting Application Payment")
                .caseLink(CaseLink.builder()
                              .caseReference("1234L")
                              .build())
                .build();

            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .claimantGaAppDetails(wrapElements(claimantCollection))
                .build();
            CaseDetails caseDetails = CaseDetails.builder().data(objectMapper.convertValue(
                caseData,
                new TypeReference<>() {
                })).build();

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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;
            respondentsResponses.add(element(respondent1Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsOfVary(YES, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForVaryJudgement(YES, YES, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(YES, NO).getCaseDetails()))
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
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Respondent Response");

        }

        @Test
        void shouldChangeStateToProceedsInHeritageWhenVaryJudgmentWhenParentIsNotClaimant() {

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;
            respondentsResponses.add(element(respondent1Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsOfVary(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForVaryJudgement(NO, YES, respondentsResponses).copy().respondentsResponses(respondentsResponses).build());
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Proceeds In Heritage");

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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;
            GARespondentResponse respondent2Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id3")
                ;
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols));
            assertThat(response.getState()).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name());
        }

        @Test
        void shouldChangeStateToApplicationDismissedWhenCOSC() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForCCJ(YES, NO));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(YES, NO).getCaseDetails()))
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
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Application Dismissed");

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Application Dismissed");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Application Dismissed");

        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetails_NotToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseData(YES, NO));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(YES, NO).getCaseDetails()))
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
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GeneralApplicationsDetails generalApp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Application Submitted - Awaiting Judicial Decision");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Application Submitted - Awaiting Judicial Decision");
        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForDirectionOrder() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_DIRECTIONS_ORDER_DOCS));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForWrittenRep() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_WRITTEN_REPRESENTATIONS));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);

        }

        @Test
        void theEndOfProcessShouldNotUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForAddlInfo() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_ADDITIONAL_INFORMATION));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(0))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoOnParentCaseGADetailsForRespondResponseState() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(YES, NO).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataByState(YES, NO, AWAITING_RESPONDENT_RESPONSE));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(YES, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(YES, NO));

            handler.handle(getCallbackParams(YES, NO));

            verify(coreCaseDataService, times(1))
                .startUpdate("1645779506193000", UPDATE_CASE_WITH_GA_STATE);
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetails_ToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseData(NO, YES));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Awaiting Respondent Response");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Awaiting Respondent Response");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Awaiting Respondent Response");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Respondent Response");
        }

        @Test
        void theEndOfProcessShouldUpdateTheStateOfGAAndAlsoUpdateStateOnParentCaseGADetailsAndCollection_ToBeNotified() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponseForCollection(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataForCollection(NO, YES));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponseForCollection(NO, YES).getCaseDetails()))
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
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getCase(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;
            GARespondentResponse respondent2Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id3")
                ;
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, NO).getCaseDetails()))
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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                    .setGeneralAppRespondent1Representative(YES)
                    .setGaRespondentDetails("id")
                    ;
            GARespondentResponse respondent2Response = new GARespondentResponse()
                    .setGeneralAppRespondent1Representative(YES)
                    .setGaRespondentDetails("id2")
                    ;
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                    .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, NO).getCaseDetails()))
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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;
            GARespondentResponse respondent2Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id3")
                ;
            respondentsResponses.add(element(respondent1Response));
            respondentsResponses.add(element(respondent2Response));
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParamsMulti(NO, NO, respondentsResponses, respondentSols).getRequest().getCaseDetails()))
                .thenReturn(getCaseMulti(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, NO).getCaseDetails()))
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

            GARespondentResponse respondent1Response = new GARespondentResponse()
                .setGeneralAppRespondent1Representative(YES)
                .setGaRespondentDetails("id")
                ;

            respondentsResponses.add(element(respondent1Response));

            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, NO).getRequest().getCaseDetails()))
                .thenReturn(getCase(respondentSols, respondentsResponses));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, NO).getCaseDetails()))
                .thenReturn(getParentCaseDataBeforeUpdate(NO, NO));

            var response = handler.handle(getCallbackParams(NO, NO));
            assertThat(response).isNotNull();
        }

        @Test
        void shouldChangeTheStateToAwaitingApplicationPaymentBeforePayment() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                .thenReturn(getSampleGeneralApplicationCaseDataBeforePayment(NO, YES));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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
                ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Awaiting Application Payment");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Awaiting Application Payment");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                .isEqualTo("Awaiting Application Payment");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                .isEqualTo("Awaiting Application Payment");
        }

        @ParameterizedTest
        @EnumSource(value = GaFinalOrderSelection.class)
        void shouldChangeTheStateToOrderMadeAfterFinalOrder(GaFinalOrderSelection selection) {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseDataAfterOrderMade(NO,
                            YES, selection, null));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Order Made");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Order Made");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                    .isEqualTo("Order Made");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(gaDetailsMasterColl.getCaseState())
                    .isEqualTo("Order Made");
        }

        @Test
        void shouldChangeTheStateToListingForAHearingAfterFinalOrder() {
            when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, YES));
            when(coreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();
            when(caseDetailsConverter.toGeneralApplicationCaseData(getCallbackParams(NO, YES).getRequest().getCaseDetails()))
                    .thenReturn(getSampleGeneralApplicationCaseDataAfterOrderMade(NO,
                            YES, GaFinalOrderSelection.ASSISTED_ORDER,
                            new AssistedOrderFurtherHearingDetails()));
            when(caseDetailsConverter.toGeneralApplicationCaseData(getStartEventResponse(NO, YES).getCaseDetails()))
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
                    ((LinkedHashMap<?, ?>) generalApplicationDetails.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalApp.getCaseState()).isEqualTo("Listed for a Hearing");

            GADetailsRespondentSol generalAppResp = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSol.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppResp.getCaseState()).isEqualTo("Listed for a Hearing");

            GADetailsRespondentSol generalAppRespTwo = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsRespondentSolTwo.getFirst()).get("value"),
                    new TypeReference<>() {});
            assertThat(generalAppRespTwo.getCaseState())
                    .isEqualTo("Listed for a Hearing");
            GeneralApplicationsDetails gaDetailsMasterColl = objectMapper.convertValue(
                    ((LinkedHashMap<?, ?>) gaDetailsMasterCollection.getFirst()).get("value"),
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
                        .paymentDetails(new PaymentDetails()
                                            .setStatus(PaymentStatus.SUCCESS)
                                            .setReference("RC-1658-4258-2679-9795")
                                            .setCustomerReference(CUSTOMER_REFERENCE)
                                            )
                        .fee(
                            new Fee()
                                .setCode("FE203")
                                .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                                .setVersion("1")
                                )
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
                    .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                            .setCaseReference(PARENT_CCD_REF.toString()))
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
                        .paymentDetails(new PaymentDetails()
                                            .setStatus(PaymentStatus.SUCCESS)
                                            .setReference("RC-1658-4258-2679-9795")
                                            .setCustomerReference(CUSTOMER_REFERENCE)
                                            )
                        .fee(
                            new Fee()
                                .setCode("FE203")
                                .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                                .setVersion("1")
                                )
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
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
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
                        .paymentDetails(new PaymentDetails()
                                            .setStatus(PaymentStatus.SUCCESS)
                                            .setReference("RC-1658-4258-2679-9795")
                                            .setCustomerReference(CUSTOMER_REFERENCE)
                                            )
                        .fee(
                            new Fee()
                                .setCode("FE203")
                                .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                                .setVersion("1")
                                )
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
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
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
                            new Fee()
                                .setCode("FE203")
                                .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                                .setVersion("1")
                                )
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
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
                .build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication(isConsented, isTobeNotified))
                    .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataForCCJ(YesOrNo isConsented, YesOrNo isTobeNotified) {
            List<GeneralApplicationTypes> types = List.of(CONFIRM_CCJ_DEBT_PAID);
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication(isConsented, isTobeNotified))
                .copy().ccdCaseReference(CHILD_CCD_REF).generalAppType(GAApplicationType.builder().types(types).build()).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataForVaryJudgement(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                                             List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = List.of(VARY_PAYMENT_TERMS_OF_JUDGMENT);

            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationVary(isConsented, isTobeNotified, respondentsResponses))
                .copy().ccdCaseReference(CHILD_CCD_REF)
                .generalAppType(GAApplicationType.builder().types(types).build()).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataMulti(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                                  List<Element<GARespondentResponse>> respondentResponses,
                                                                  List<Element<GASolicitorDetailsGAspec>> respondentDetails) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationMulti(isConsented, isTobeNotified, respondentResponses, respondentDetails))
                .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataByState(YesOrNo isConsented, YesOrNo isTobeNotified, CaseState caseState) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplicationByState(
                    getGeneralApplication(isConsented, isTobeNotified), caseState)
                .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataForCollection(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGaForCollection(
                    getGeneralApplication(isConsented, isTobeNotified))
                .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataBeforePayment(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplicationBeforePayment(isConsented, isTobeNotified))
                .copy().ccdCaseReference(CHILD_CCD_REF).build();
        }

        private GeneralApplicationCaseData getSampleGeneralApplicationCaseDataAfterOrderMade(
                YesOrNo isConsented,
                YesOrNo isTobeNotified,
                GaFinalOrderSelection selection,
                AssistedOrderFurtherHearingDetails hearingDetails) {
            return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                            getGeneralApplication(isConsented, isTobeNotified))
                    .copy().ccdCaseReference(CHILD_CCD_REF)
                    .finalOrderSelection(selection).assistedOrderFurtherHearingDetails(hearingDetails).build();
        }

        private CallbackParams getCallbackParamsMulti(YesOrNo isConsented, YesOrNo isTobeNotified,
                                                      List<Element<GARespondentResponse>> respondentResponses,
                                                      List<Element<GASolicitorDetailsGAspec>> respondentDetails) {
            return new CallbackParams()
                .type(ABOUT_TO_SUBMIT)
                .pageId(null)
                .request(CallbackRequest.builder()
                             .caseDetails(CaseDetails.builder()
                                              .data(objectMapper.convertValue(
                                                  getSampleGeneralApplicationCaseDataMulti(isConsented, isTobeNotified,
                                                                                           respondentResponses, respondentDetails),
                                                  new TypeReference<>() {
                                                  })).id(CASE_ID).build())
                             .eventId("END_BUSINESS_PROCESS_GASPEC")
                             .build())
                .caseData(getSampleGeneralApplicationCaseDataMulti(isConsented, isTobeNotified,
                                                                   respondentResponses, respondentDetails))
                .version(null)
                .params(null);
        }

        private CallbackParams getCallbackParamsOfVary(YesOrNo isConsented, YesOrNo isTobeNotified) {
            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
            return new CallbackParams()
                .type(ABOUT_TO_SUBMIT)
                .pageId(null)
                .request(CallbackRequest.builder()
                             .caseDetails(CaseDetails.builder()
                                              .data(objectMapper.convertValue(
                                                  getSampleGeneralApplicationCaseDataForVaryJudgement(isConsented, isTobeNotified, respondentsResponses),
                                                  new TypeReference<>() {
                                                  })).id(CASE_ID).build())
                             .eventId("END_BUSINESS_PROCESS_GASPEC")
                             .build())
                .caseData(getSampleGeneralApplicationCaseDataForVaryJudgement(isConsented, isTobeNotified, respondentsResponses))
                .version(null)
                .params(null);
        }

        private CallbackParams getCallbackParams(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return new CallbackParams()
                    .type(ABOUT_TO_SUBMIT)
                    .pageId(null)
                    .request(CallbackRequest.builder()
                            .caseDetails(CaseDetails.builder()
                                    .data(objectMapper.convertValue(
                                            getSampleGeneralApplicationCaseData(isConsented, isTobeNotified),
                                            new TypeReference<>() {
                                            })).id(CASE_ID).build())
                            .eventId("END_BUSINESS_PROCESS_GASPEC")
                            .build())
                    .caseData(getSampleGeneralApplicationCaseData(isConsented, isTobeNotified))
                    .version(null)
                    .params(null);
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

        private GeneralApplicationCaseData getParentCaseDataBeforeUpdate(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return new GeneralApplicationCaseData()
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

        private GeneralApplicationCaseData getParentCaseDataBeforeUpdateCollection(YesOrNo isConsented, YesOrNo isTobeNotified) {
            return new GeneralApplicationCaseData()
                .generalApplications(wrapElements(getGeneralApplication(isConsented, isTobeNotified)))
                .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                       .caseLink(CaseLink.builder().caseReference(CHILD_CCD_REF.toString()).build())
                                                       .caseState("Awaiting Application Payment")
                                                       .build()))
                .build();
        }

        private GeneralApplicationCaseData getCase(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                                 List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            DynamicList dynamicListTest = fromList(getSampleCourLocations());
            Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
            first.ifPresent(dynamicListTest::setValue);

            return new GeneralApplicationCaseData()
                .ccdCaseReference(CHILD_CCD_REF)
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setPaymentDetails(new PaymentDetails()
                                                                                .setCustomerReference("1336546")
                                                                                ))
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
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .build();
        }

        private GeneralApplicationCaseData getCaseMulti(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                                 List<Element<GARespondentResponse>> respondentsResponses) {
            List<GeneralApplicationTypes> types = List.of(
                (RELIEF_FROM_SANCTIONS));
            DynamicList dynamicListTest = fromList(getSampleCourLocations());
            Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
            first.ifPresent(dynamicListTest::setValue);

            return new GeneralApplicationCaseData()
                .ccdCaseReference(CHILD_CCD_REF)
                .ccdState(PENDING_APPLICATION_ISSUED)
                .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                              .setCaseReference(PARENT_CCD_REF.toString()))
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setPaymentDetails(new PaymentDetails()
                                                                                .setCustomerReference("1336546")
                                                                                ))
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
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
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
