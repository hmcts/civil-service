package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;

@Service
@RequiredArgsConstructor
public class ApplicationsProceedOfflineNotificationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT,
                CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT);
    public static final String TASK_ID_CLAIMANT = "claimantLipApplicationOfflineDashboardNotification";
    public static final String TASK_ID_DEFENDANT = "defendantLipApplicationOfflineDashboardNotification";
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationService dashboardNotificationService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;
    private static final String CLAIMANT = "Claimant";
    private static final String DEFENDANT = "Defendant";

    private static final List<String> NON_LIVE_STATES = List.of(
        "Application Closed",
        "Order Made",
        "Application Dismissed"
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isLipVLipEnabled()
            && featureToggleService.isGeneralApplicationsEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForProceedOffline)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT.name())) {
            return TASK_ID_CLAIMANT;
        } else {
            return TASK_ID_DEFENDANT;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse configureScenarioForProceedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!caseData.getCcdState().equals(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String notificationType = notificationType(callbackParams);
        if (notificationType == null) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        ScenarioRequestParams notificationParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        dashboardScenariosService.recordScenarios(
            authToken,
            notificationType.equals(CLAIMANT)
                ? SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()
                : SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            notificationParams);
        if (caseData.getGeneralApplications() == null || caseData.getGeneralApplications().isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }
        if (isApplicationsExistLive(caseData, notificationType)) {
            dashboardScenariosService.recordScenarios(
                authToken,
                notificationType.equals(CLAIMANT)
                    ? SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario()
                    : SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                notificationParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String notificationType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String eventId = callbackParams.getRequest().getEventId();
        if (eventId.equals(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT.name())
            && caseData.isApplicantLiP()) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseData.getCcdCaseReference().toString(), CLAIMANT);
            return CLAIMANT;
        } else if (eventId.equals(CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT.name())
            && caseData.isRespondent1LiP()) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseData.getCcdCaseReference().toString(), DEFENDANT);
            return DEFENDANT;
        }
        return null;
    }

    private boolean isApplicationsExistLive(CaseData caseData, String notificationType) {
        if (notificationType.equals(CLAIMANT) && isClaimantApplicationValid(caseData)) {
            return true;
        }
        return notificationType.equals(DEFENDANT) && isDefendantApplicationValid(caseData);
    }

    private boolean isClaimantApplicationValid(CaseData caseData) {
        List<Element<GeneralApplicationsDetails>> claimantGaAppDetails = caseData.getClaimantGaAppDetails();
        if (claimantGaAppDetails != null && !claimantGaAppDetails.isEmpty()) {
            for (Element<GeneralApplicationsDetails> element : claimantGaAppDetails) {
                if (!NON_LIVE_STATES.contains(element.getValue().getCaseState())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDefendantApplicationValid(CaseData caseData) {
        List<Element<GADetailsRespondentSol>> defendantGaAppDetails = caseData.getRespondentSolGaAppDetails();
        if (defendantGaAppDetails != null && !defendantGaAppDetails.isEmpty()) {
            for (Element<GADetailsRespondentSol> element : defendantGaAppDetails) {
                if (!NON_LIVE_STATES.contains(element.getValue().getCaseState())) {
                    return true;
                }
            }
        }
        return false;
    }
}
