package uk.gov.hmcts.reform.civil.handler.callback.user.ga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackParams.Params;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.ga.GaInitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import static org.mockito.Mockito.lenient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;

@ExtendWith(MockitoExtension.class)
class GaInitiateGeneralApplicationHandlerTest extends BaseCallbackHandlerTest {

    private static final String BEARER = "token";

    @Mock
    private GaInitiateGeneralApplicationService gaInitiateGeneralApplicationService;
    @Mock
    private GeneralApplicationValidator generalApplicationValidator;
    @Mock
    private GeneralAppFeesService feesService;
    @Mock
    private LocationReferenceDataService locationReferenceDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private GaInitiateGeneralApplicationSubmittedHandler submittedHandler;
    @Mock
    private UserService localUserService;

    private GaInitiateGeneralApplicationHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        handler = new GaInitiateGeneralApplicationHandler(
            gaInitiateGeneralApplicationService,
            generalApplicationValidator,
            objectMapper,
            localUserService,
            feesService,
            locationReferenceDataService,
            featureToggleService,
            coreCaseUserService,
            submittedHandler
        );

        lenient().when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        lenient().when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        lenient().when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(true);
        lenient().when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);
        lenient().when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);
        lenient().when(gaInitiateGeneralApplicationService.ensureDefaults(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void aboutToStart_shouldAddErrorWhenRespondentNotAssigned() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withGeneralAppType(GAApplicationType.builder()
                .types(List.of(uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME))
                .build())
            .build();
        CaseData caseData = CaseDataBuilder.builder()
            .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
            .build();

        when(gaInitiateGeneralApplicationService.asCaseData(gaCaseData)).thenReturn(caseData);
        when(gaInitiateGeneralApplicationService.respondentAssigned(gaCaseData)).thenReturn(false);
        when(gaInitiateGeneralApplicationService.caseContainsLip(gaCaseData)).thenReturn(false);
        when(locationReferenceDataService.getCourtLocationsForGeneralApplication(any()))
            .thenReturn(List.of(LocationRefData.builder()
                .siteName("Birmingham")
                .courtAddress("Priory Ct")
                .postcode("B4 7PS")
                .build()));

        CallbackParams params = gaCallbackParams(gaCaseData, caseData, CallbackType.ABOUT_TO_START);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).contains("Application cannot be created until all the required "
            + "respondent solicitor are assigned to the case.");
        assertThat(response.getData()).containsKey("generalAppHearingDetails");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setFeesAndPba_shouldPopulateFeeDetails() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withGeneralAppType(GAApplicationType.builder()
                .types(List.of(uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME))
                .build())
            .build();
        CaseData caseData = CaseDataBuilder.builder().build();
        Fee fee = Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1000)).code("CODE").version("1").build();

        lenient().when(gaInitiateGeneralApplicationService.asCaseData(any(GeneralApplicationCaseData.class))).thenReturn(caseData);
        when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class))).thenReturn(fee);

        CallbackParams params = gaCallbackParams(gaCaseData, caseData, CallbackType.MID, "ga-fees-and-pba");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Map<?, ?> feeDetails = (Map<?, ?>) response.getData().get("generalAppPBADetails");
        assertThat(feeDetails).isNotNull();
        assertThat(feeDetails.get("generalAppFeeToPayInText").toString()).startsWith("£");
        Map<?, ?> feeMap = (Map<?, ?>) feeDetails.get("fee");
        assertThat(feeMap.get("code")).isEqualTo("CODE");
        assertThat(feeMap.get("version")).isEqualTo("1");
    }

    @Test
    void submitApplication_shouldReturnBuiltGaData() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withGeneralAppType(GAApplicationType.builder()
                .types(List.of(uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME))
                .build())
            .withIsGaApplicantLip(YesOrNo.NO)
            .build();
        CaseData caseData = CaseDataBuilder.builder().build();
        GeneralApplicationCaseData builtGa = gaCaseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                .types(List.of(uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME))
                .build())
            .build();

        lenient().when(gaInitiateGeneralApplicationService.asCaseData(any(GeneralApplicationCaseData.class))).thenReturn(caseData);
        when(feesService.getFeeForGA(any(GeneralApplicationCaseData.class))).thenReturn(Fee.builder().build());
        when(gaInitiateGeneralApplicationService.buildCaseData(any(GeneralApplicationCaseData.class), any(UserDetails.class), any()))
            .thenReturn(builtGa);
        when(localUserService.getUserDetails(any())).thenReturn(UserDetails.builder().id("id").build());

        CallbackParams params = gaCallbackParams(gaCaseData, caseData, CallbackType.ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).isEqualTo(builtGa.toMap(objectMapper));
    }

    @Test
    void submitted_shouldDelegateToSubmittedHandler() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        CaseData caseData = CaseDataBuilder.builder().build();
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder().build();
        when(submittedHandler.handle(any())).thenReturn(expected);

        CallbackParams params = gaCallbackParams(gaCaseData, caseData, CallbackType.SUBMITTED);

        assertThat(handler.handle(params)).isSameAs(expected);
        verify(submittedHandler).handle(params);
        verify(gaInitiateGeneralApplicationService, never()).asCaseData(any());
    }

    private CallbackParams gaCallbackParams(GeneralApplicationCaseData gaCaseData,
                                            CaseData caseData,
                                            CallbackType type) {

        return CallbackParams.builder()
            .type(type)
            .gaCaseData(gaCaseData)
            .caseData(caseData)
            .params(Map.of(Params.BEARER_TOKEN, BEARER))
            .request(CallbackRequest.builder()
                .eventId(INITIATE_GENERAL_APPLICATION.name())
                .caseDetails(CaseDetails.builder()
                    .id(CASE_ID)
                    .data(Map.of())
                    .state(CaseState.AWAITING_APPLICATION_PAYMENT.name())
                    .build())
                .build())
            .build();
    }

    private CallbackParams gaCallbackParams(GeneralApplicationCaseData gaCaseData,
                                            CaseData caseData,
                                            CallbackType type,
                                            String pageId) {
        return CallbackParams.builder()
            .type(type)
            .pageId(pageId)
            .gaCaseData(gaCaseData)
            .caseData(caseData)
            .params(Map.of(Params.BEARER_TOKEN, BEARER))
            .request(CallbackRequest.builder()
                .eventId(INITIATE_GENERAL_APPLICATION.name())
                .caseDetails(CaseDetails.builder()
                    .id(CASE_ID)
                    .data(Map.of())
                    .state(CaseState.AWAITING_APPLICATION_PAYMENT.name())
                    .build())
                .build())
            .build();
    }
}
