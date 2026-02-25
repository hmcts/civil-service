package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.ga.service.StateGeneratorService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_UNCLOAKED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_UNCLOAKED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ModifyStateAfterAdditionalFeeReceivedCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    public static final long CCD_CASE_REFERENCE = 1234L;
    public static final String PARENT_CASE_REFERENCE = "123498";

    @Mock
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Mock
    private StateGeneratorService stateGeneratorService;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private AssignCaseToRespondentSolHelper assignCaseToRespondentSolHelper;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private GaForLipService gaForLipService;

    @InjectMocks
    private ModifyStateAfterAdditionalFeeReceivedCallbackHandler handler;

    private final List<MakeAppAvailableCheckGAspec> makeAppAvailableCheck = List.of(MakeAppAvailableCheckGAspec.CONSENT_AGREEMENT_CHECKBOX);

    private final GAMakeApplicationAvailableCheck gaMakeApplicationAvailableCheck = new GAMakeApplicationAvailableCheck()
        .setMakeAppAvailableCheck(makeAppAvailableCheck);

    @Test
    void shouldRespondWithStateChanged() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(YesOrNo.NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(NO)
            .isGaRespondentTwoLip(NO)
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AWAITING_RESPONDENT_RESPONSE.toString());

        verify(assignCaseToRespondentSolHelper, times(1)).assignCaseToRespondentSolicitor(
            any(),
            any()
        );
    }

    @Test
    void shouldRespondWithStateChangedWhenApplicationUncloaked() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(YesOrNo.NO)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY))
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(NO)
            .isGaRespondentTwoLip(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AWAITING_RESPONDENT_RESPONSE.toString());

        verify(assignCaseToRespondentSolHelper, times(1)).assignCaseToRespondentSolicitor(
            any(),
            any()
        );
    }

    @Test
    void shouldNotRespondWithStateChangedWhenApplicationUncloaked() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION))
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AWAITING_RESPONDENT_RESPONSE.toString());

        verify(assignCaseToRespondentSolHelper, times(0)).assignCaseToRespondentSolicitor(
            any(),
            any()
        );
    }

    @Test
    void shouldDispatchBusinessProcess_whenStatusIsReady() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy().parentCaseReference("1234").build();
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1)).updateParentApplicationVisibilityWithNewState(
            caseData,
            AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()
        );
    }

    @Test
    void shouldUpdateTaskListActionNeeded_whenInvoked() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy()
            .parentCaseReference("1234")
            .claimantGaAppDetails(List.of(Element.<GeneralApplicationsDetails>builder()
                                              .value(GeneralApplicationsDetails.builder()
                                                         .parentClaimantIsApplicant(YES)
                                                         .caseState(AWAITING_APPLICATION_PAYMENT.getDisplayedValue())
                                                         .build())
                                              .build()))
            .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                   .value(GADetailsRespondentSol.builder()
                                                              .parentClaimantIsApplicant(YES)
                                                              .caseState(AWAITING_RESPONDENT_RESPONSE.getDisplayedValue())
                                                              .build())
                                                   .build()))
            .build();
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        HashMap<String, Object> scenarioParams = new HashMap<>();

        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1)).updateParentApplicationVisibilityWithNewState(
            caseData,
            AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateTaskListInProgress_whenInvoked() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy()
            .parentCaseReference("1234")
            .claimantGaAppDetails(List.of(Element.<GeneralApplicationsDetails>builder()
                                              .value(GeneralApplicationsDetails.builder()
                                                         .parentClaimantIsApplicant(YES)
                                                         .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.getDisplayedValue())
                                                         .build())
                                              .build()))
            .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                   .value(GADetailsRespondentSol.builder()
                                                              .parentClaimantIsApplicant(YES)
                                                              .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.getDisplayedValue())
                                                              .build())
                                                   .build()))
            .build();
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        HashMap<String, Object> scenarioParams = new HashMap<>();

        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1)).updateParentApplicationVisibilityWithNewState(
            caseData,
            AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateTaskListAvailable_whenInvoked() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy()
            .parentCaseReference("1234")
            .claimantGaAppDetails(List.of(Element.<GeneralApplicationsDetails>builder()
                                              .value(GeneralApplicationsDetails.builder()
                                                         .parentClaimantIsApplicant(YES)
                                                         .caseState(APPLICATION_CLOSED.getDisplayedValue())
                                                         .build())
                                              .build()))
            .respondentSolGaAppDetails(List.of(Element.<GADetailsRespondentSol>builder()
                                                   .value(GADetailsRespondentSol.builder()
                                                              .parentClaimantIsApplicant(YES)
                                                              .caseState(APPLICATION_CLOSED.getDisplayedValue())
                                                              .build())
                                                   .build()))
            .build();
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        HashMap<String, Object> scenarioParams = new HashMap<>();

        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1)).updateParentApplicationVisibilityWithNewState(
            caseData,
            AWAITING_RESPONDENT_RESPONSE.getDisplayedValue()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);
    }

    @Test
    void shouldUpdateDefendantTaskListIfGaRespondentLip() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(YesOrNo.NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(YES)
            .parentClaimantIsApplicant(YES)
            .isGaApplicantLip(NO)
            .ccdCaseReference(CCD_CASE_REFERENCE).build()
            .copy().parentCaseReference("1234").build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getParentCaseReference(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateClaimantTaskListIfGaApplicantLipAndFeeIsPaidPartialRemission() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(YES)
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForAdditionalFee(NO))
            .additionalHwfDetails(new HelpWithFeesDetails().setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA))
            .generalAppHelpWithFees(
                new HelpWithFees()
                    .setHelpWithFeesReferenceNumber("ABC-DEF-IJK")
                    .setHelpWithFee(YES))
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateClaimantTaskListIfGaApplicantLipAndFeeIsPaidNoRemission() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(YES)
            .additionalHwfDetails(new HelpWithFeesDetails().setHwfCaseEvent(NO_REMISSION_HWF_GA))
            .generalAppHelpWithFees(
                new HelpWithFees()
                    .setHelpWithFeesReferenceNumber("ABC-DEF-IJK")
                    .setHelpWithFee(YES))
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateClaimantTaskListIfGaApplicantLipAndFeeIsPaidThroughWhenHwfIsRejected() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(YES)
            .generalAppHelpWithFees(
                new HelpWithFees()
                    .setHelpWithFeesReferenceNumber("ABC-DEF-IJK")
                    .setHelpWithFee(YES))
            .additionalHwfDetails(new HelpWithFeesDetails().setHwfCaseEvent(NO_REMISSION_HWF_GA))
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldUpdateClaimantTaskListIfGaApplicantLipAndFeeIsPaidFullRemission() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .isGaRespondentOneLip(NO)
            .isGaApplicantLip(YES)
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForGa(YES))
            .generalAppHelpWithFees(
                new HelpWithFees()
                    .setHelpWithFeesReferenceNumber("ABC-DEF-IJK")
                    .setHelpWithFee(YES))
            .additionalHwfDetails(new HelpWithFeesDetails().setHwfCaseEvent(FULL_REMISSION_HWF_GA))
            .ccdCaseReference(CCD_CASE_REFERENCE).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotUpdateIfGaApplicantOrRespondentNotLip() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(YesOrNo.NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                          .email("test@gmail.com").organisationIdentifier("org1").build())
            .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
            .ccdCaseReference(CCD_CASE_REFERENCE)
            .isGaApplicantLip(NO)
            .isGaRespondentOneLip(NO).build();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(false);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        handler.handle(params);

        verifyNoInteractions(dashboardApiClient);
    }

    @Test
    void shouldCreateDashboardNotificationForRespondentWhenJudgeUncloaksNonUrgentApplication() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(NO)
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setRequestMoreInfoOption(
                                                     GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY))
            .ccdCaseReference(CCD_CASE_REFERENCE).build().copy()
            .parentCaseReference(PARENT_CASE_REFERENCE)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build()).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_UNCLOAKED_RESPONDENT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldCreateDashboardNotificationForRespondentWhenJudgeUncloaksUrgentApplication() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .isMultiParty(NO)
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setRequestMoreInfoOption(
                                                     GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY))
            .ccdCaseReference(CCD_CASE_REFERENCE).build().copy()
            .parentCaseReference(PARENT_CASE_REFERENCE)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build()).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(any()))
            .thenReturn(AWAITING_RESPONDENT_RESPONSE);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        handler.handle(params);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_UNCLOAKED_RESPONDENT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotUpdateApplication_whenPaymentFailed() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy()
            .parentCaseReference("1234")
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setStatus(PaymentStatus.FAILED)))
            .build();
        when(gaForLipService.isLipApp(caseData)).thenReturn(true);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        handler.handle(params);

        verifyNoInteractions(dashboardApiClient);
    }

    @Test
    void shouldNotUpdateParent_whenPaymentFailed() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(CCD_CASE_REFERENCE).build();
        caseData = caseData.copy()
            .parentCaseReference("1234")
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setStatus(PaymentStatus.FAILED)))
            .build();
        when(gaForLipService.isLipApp(caseData)).thenReturn(true);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        handler.handle(params);

        verifyNoInteractions(parentCaseUpdateHelper);
        verifyNoInteractions(dashboardApiClient);
    }

    public List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email("test@gmail.com").organisationIdentifier("org3").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        return respondentSols;
    }
}
