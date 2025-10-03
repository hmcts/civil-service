package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.service.AssignCaseToResopondentSolHelper;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.service.StateGeneratorService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.APPLICANT_ACTION_NEEDED_GA_STATES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.APPLICANT_IN_PROGRESS_GA_STATES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.RESPONDENT_ACTION_NEEDED_GA_STATES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.RESPONDENT_IN_PROGRESS_GA_STATES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModifyStateAfterAdditionalFeeReceivedCallbackHandler extends CallbackHandler {

    private final ParentCaseUpdateHelper parentCaseUpdateHelper;
    private final StateGeneratorService stateGeneratorService;
    private final AssignCaseToResopondentSolHelper assignCaseToResopondentSolHelper;
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;
    private final GaForLipService gaForLipService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final List<CaseEvent> EVENTS = singletonList(MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::changeApplicationState,
            callbackKey(SUBMITTED), this::changeGADetailsStatusInParent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse changeApplicationState(CallbackParams callbackParams) {
        Long caseId = callbackParams.getCaseData().getCcdCaseReference();
        CaseData caseData = callbackParams.getCaseData();
        // Do not progress the application if payment not successful
        if (gaForLipService.isLipApp(caseData) && getPaymentStatus(caseData) == PaymentStatus.FAILED) {
            log.info("Payment status is failed for caseId: {}", caseData.getCcdCaseReference());
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String newCaseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData).toString();
        log.info("Changing state to {} for caseId: {}", newCaseState, caseId);

        if (caseData.getMakeAppVisibleToRespondents() != null
            || isApplicationUncloakedForRequestMoreInformation(caseData).equals(YES)) {
            assignCaseToResopondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId.toString());
            updateDashboardTaskListAndNotification(
                callbackParams,
                getScenariosAsList(getDashboardNotificationRespondentScenario(caseData)),
                                                   caseData.getCcdCaseReference().toString());
        }

        updateDashboardTaskListAndNotification(callbackParams, getDashboardNotificationScenarioForApplicant(caseData), caseData.getCcdCaseReference().toString());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(newCaseState)
            .build();
    }

    private void updateDashboardTaskListAndNotification(CallbackParams callbackParams, List<String> scenarios,
                                                        String caseReference) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData caseData = callbackParams.getCaseData();
        if (gaForLipService.isGaForLip(caseData)) {
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                    caseData)).build();
            if (scenarios != null) {
                scenarios.forEach(scenario -> dashboardScenariosService.recordScenarios(
                        authToken,
                        scenario,
                        caseReference,
                        scenarioParams
                ));
            }
        }
    }

    private List<String> getDashboardNotificationScenarioForApplicant(CaseData caseData) {
        List<String> scenarios = new ArrayList<>();
        if (caseData.getIsGaApplicantLip() == YES
            && (Objects.nonNull(caseData.getAdditionalHwfDetails()))) {
            if (caseData.gaAdditionalFeeFullRemissionNotGrantedHWF(caseData)) {
                scenarios.add(SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario());
            } else {
                scenarios.add(SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT.getScenario());
            }
        }

        scenarios.add(SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario());
        return scenarios;
    }

    private YesOrNo isApplicationUncloakedForRequestMoreInformation(CaseData caseData) {
        if (caseData.getJudicialDecisionRequestMoreInfo() != null
            && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() != null
            && caseData.getJudicialDecisionRequestMoreInfo()
            .getRequestMoreInfoOption().equals(SEND_APP_TO_OTHER_PARTY)) {
            return YES;
        }
        return NO;
    }

    private CallbackResponse changeGADetailsStatusInParent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // Do not progress the application if payment not successful
        if (gaForLipService.isLipApp(caseData) && getPaymentStatus(caseData) == PaymentStatus.FAILED) {
            log.info("Payment status is failed for caseId: {}", caseData.getCcdCaseReference());
            return SubmittedCallbackResponse.builder().build();
        }
        String newCaseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData)
            .getDisplayedValue();
        log.info("Updating parent with latest state {} of application-caseId: {}",
                 newCaseState, caseData.getCcdCaseReference()
        );
        parentCaseUpdateHelper.updateParentApplicationVisibilityWithNewState(
            caseData,
            newCaseState
        );
        updateTaskListClaimantAndDefendant(callbackParams, caseData);
        return SubmittedCallbackResponse.builder().build();
    }

    private PaymentStatus getPaymentStatus(CaseData caseData) {
        return Optional.of(caseData)
            .map(CaseData::getGeneralAppPBADetails)
            .map(GAPbaDetails::getAdditionalPaymentDetails)
            .map(PaymentDetails::getStatus).orElse(null);
    }

    private void updateTaskListClaimantAndDefendant(CallbackParams callbackParams, CaseData caseData) {
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseData.getParentCaseReference()));
        CaseData parentCaseData = caseDetailsConverter.toCaseDataGA(caseDetails);
        String claimantScenario = null;
        String defendantScenario = null;
        boolean someGaActionNeededClaimant = Optional.ofNullable(parentCaseData.getClaimantGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? APPLICANT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState())
                : RESPONDENT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState()));
        boolean someGaActionNeededDefendant = Optional.ofNullable(parentCaseData.getRespondentSolGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? RESPONDENT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState())
                : APPLICANT_ACTION_NEEDED_GA_STATES.contains(gaDetails.getCaseState()));
        if (someGaActionNeededClaimant) {
            claimantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_CLAIMANT.getScenario();
        }
        if (someGaActionNeededDefendant) {
            defendantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario();
        }
        boolean someGaInProgressClaimant = claimantScenario == null
            && Optional.ofNullable(parentCaseData.getClaimantGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? APPLICANT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState())
                : RESPONDENT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState()));
        boolean someGaInProgressDefendant = defendantScenario == null
            && Optional.ofNullable(parentCaseData.getRespondentSolGaAppDetails()).orElse(Collections.emptyList()).stream()
            .map(Element::getValue)
            .anyMatch(gaDetails -> gaDetails.getParentClaimantIsApplicant() == YesOrNo.YES
                ? RESPONDENT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState())
                : APPLICANT_IN_PROGRESS_GA_STATES.contains(gaDetails.getCaseState()));
        if (someGaInProgressClaimant) {
            claimantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_CLAIMANT.getScenario();
        }
        if (someGaInProgressDefendant) {
            defendantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario();
        }
        if (claimantScenario == null && parentCaseData.getClaimantGaAppDetails() != null
            && parentCaseData.getClaimantGaAppDetails().size() > 0) {
            claimantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario();
        }
        if (defendantScenario == null && parentCaseData.getRespondentSolGaAppDetails() != null
            && parentCaseData.getRespondentSolGaAppDetails().size() > 0) {
            defendantScenario = SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario();
        }
        updateDashboardTaskListAndNotification(
            callbackParams,
            getScenariosAsList(claimantScenario),
            caseData.getParentCaseReference()
        );
        updateDashboardTaskListAndNotification(
            callbackParams,
            getScenariosAsList(defendantScenario),
            caseData.getParentCaseReference()
        );
    }

    private String getDashboardNotificationRespondentScenario(CaseData caseData) {
        if (caseData.isUrgent()) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_UNCLOAKED_RESPONDENT.getScenario();
        } else {
            return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_UNCLOAKED_RESPONDENT.getScenario();
        }
    }

    private List<String> getScenariosAsList(String scenario) {
        return Optional.ofNullable(scenario)
            .map(List::of)
            .orElse(Collections.emptyList());
    }
}
