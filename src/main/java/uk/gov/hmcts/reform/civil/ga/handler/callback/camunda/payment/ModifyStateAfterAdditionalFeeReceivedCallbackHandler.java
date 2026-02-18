package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.ga.service.StateGeneratorService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
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
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.APPLICANT_ACTION_NEEDED_GA_STATES;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.APPLICANT_IN_PROGRESS_GA_STATES;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.RESPONDENT_ACTION_NEEDED_GA_STATES;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.TaskListUpdateHandler.RESPONDENT_IN_PROGRESS_GA_STATES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModifyStateAfterAdditionalFeeReceivedCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ParentCaseUpdateHelper parentCaseUpdateHelper;
    private final StateGeneratorService stateGeneratorService;
    private final AssignCaseToRespondentSolHelper assignCaseToRespondentSolHelper;
    private final DashboardApiClient dashboardApiClient;
    private final GaDashboardNotificationsParamsMapper mapper;
    private final GaForLipService gaForLipService;
    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final List<CaseEvent> EVENTS = singletonList(MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit,
            callbackKey(SUBMITTED), this::handleSubmitted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        Long caseId = caseData.getCcdCaseReference();
        // Do not progress the application if payment not successful
        if (isPaymentFailed(caseData)) {
            log.info("Payment status is failed for caseId: {}", caseData.getCcdCaseReference());
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String newCaseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData).toString();
        log.info("Changing state to {} for caseId: {}", newCaseState, caseId);

        if (caseData.getMakeAppVisibleToRespondents() != null
            || isApplicationUncloakedByJudge(caseData)) {
            assignCaseToRespondentSolicitor(caseData, caseId.toString());
            recordDashboardScenarios(
                callbackParams,
                getScenariosAsList(getRespondentScenario(caseData)),
                                                   caseData.getCcdCaseReference().toString());
        }

        recordDashboardScenarios(callbackParams, getApplicantScenarios(caseData), caseData.getCcdCaseReference().toString());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(newCaseState)
            .build();
    }

    private CallbackResponse handleSubmitted(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        // Do not progress the application if payment not successful
        if (isPaymentFailed(caseData)) {
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
        updateParentCaseTaskList(callbackParams, caseData);
        return SubmittedCallbackResponse.builder().build();
    }

    private void recordDashboardScenarios(CallbackParams callbackParams, List<String> scenarios,
                                          String caseReference) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        if (gaForLipService.isGaForLip(caseData)) {
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                caseData)).build();
            scenarios.forEach(scenario -> dashboardApiClient.recordScenario(
                caseReference,
                scenario,
                authToken,
                scenarioParams
            ));
        }
    }

    private List<String> getApplicantScenarios(GeneralApplicationCaseData caseData) {
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

    private String getRespondentScenario(GeneralApplicationCaseData caseData) {
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

    private void updateParentCaseTaskList(CallbackParams callbackParams, GeneralApplicationCaseData caseData) {
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseData.getParentCaseReference()));
        GeneralApplicationCaseData parentCaseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);

        String claimantScenario = getScenarioForParty(parentCaseData.getClaimantGaAppDetails(), true);
        String defendantScenario = getScenarioForParty(parentCaseData.getRespondentSolGaAppDetails(), false);

        recordDashboardScenarios(
            callbackParams,
            getScenariosAsList(claimantScenario),
            caseData.getParentCaseReference()
        );
        recordDashboardScenarios(
            callbackParams,
            getScenariosAsList(defendantScenario),
            caseData.getParentCaseReference()
        );
    }

    private String getScenarioForParty(List<? extends Element<?>> gaAppDetails, boolean isClaimant) {
        if (gaAppDetails == null || gaAppDetails.isEmpty()) {
            return null;
        }

        List<GaSummary> summaries = gaAppDetails.stream()
            .map(Element::getValue)
            .map(this::toSummary)
            .filter(Objects::nonNull)
            .toList();

        if (hasMatchingGaState(summaries, isClaimant, APPLICANT_ACTION_NEEDED_GA_STATES, RESPONDENT_ACTION_NEEDED_GA_STATES)) {
            return isClaimant
                ? SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_CLAIMANT.getScenario()
                : SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT.getScenario();
        }

        if (hasMatchingGaState(summaries, isClaimant, APPLICANT_IN_PROGRESS_GA_STATES, RESPONDENT_IN_PROGRESS_GA_STATES)) {
            return isClaimant
                ? SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_CLAIMANT.getScenario()
                : SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT.getScenario();
        }

        return isClaimant
            ? SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario()
            : SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario();
    }

    private record GaSummary(String caseState, YesOrNo parentClaimantIsApplicant) { }

    private GaSummary toSummary(Object gaDetails) {
        return switch (gaDetails) {
            case GeneralApplicationsDetails d -> new GaSummary(d.getCaseState(), d.getParentClaimantIsApplicant());
            case GADetailsRespondentSol d -> new GaSummary(d.getCaseState(), d.getParentClaimantIsApplicant());
            default -> null;
        };
    }

    private boolean hasMatchingGaState(List<GaSummary> summaries, boolean isClaimant,
                                       List<String> applicantStates, List<String> respondentStates) {
        return summaries.stream().anyMatch(s -> {
            boolean applicantPerspective = s.parentClaimantIsApplicant() == YesOrNo.YES;
            List<String> target = (isClaimant == applicantPerspective) ? applicantStates : respondentStates;
            return target.contains(s.caseState());
        });
    }

    private void assignCaseToRespondentSolicitor(GeneralApplicationCaseData caseData, String caseId) {
        assignCaseToRespondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId);
    }

    private boolean isPaymentFailed(GeneralApplicationCaseData caseData) {
        return gaForLipService.isLipApp(caseData) && getPaymentStatus(caseData) == PaymentStatus.FAILED;
    }

    private PaymentStatus getPaymentStatus(GeneralApplicationCaseData caseData) {
        return Optional.of(caseData)
            .map(GeneralApplicationCaseData::getGeneralAppPBADetails)
            .map(GeneralApplicationPbaDetails::getAdditionalPaymentDetails)
            .map(PaymentDetails::getStatus).orElse(null);
    }

    private boolean isApplicationUncloakedByJudge(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo())
            .map(uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo::getRequestMoreInfoOption)
            .map(option -> option == SEND_APP_TO_OTHER_PARTY)
            .orElse(false);
    }
}
