package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT;

@Service
public class StayLiftedDefendantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_STAY_LIFTED_DEFENDANT);
    public static final String TASK_ID = "DashboardNotificationStayLiftedDefendant";

    public StayLiftedDefendantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                  DashboardNotificationsParamsMapper mapper,
                                                  FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    public Map<String, Boolean> getScenarios(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return Map.of(
                SCENARIO_AAA6_CP_STAY_LIFTED_DEFENDANT.getScenario(), true,
                SCENARIO_AAA6_CP_STAY_LIFTED_RESET_HEARING_TASKS_DEFENDANT.getScenario(), hadHearingScheduled(caseData),
                getViewDocumentsScenario(caseData).getScenario(), !isPreCaseProgression(caseData)
            );
        }

        return new HashMap<>();
    }

    private boolean hadHearingScheduled(CaseData caseData) {
        return List.of(
            HEARING_READINESS,
            PREPARE_FOR_HEARING_CONDUCT_HEARING
        ).contains(CaseState.valueOf(caseData.getPreStayState()));
    }

    private boolean isPreCaseProgression(CaseData caseData) {
        return List.of(
            JUDICIAL_REFERRAL,
            IN_MEDIATION
        ).contains(CaseState.valueOf(caseData.getPreStayState()));
    }

    private DashboardScenarios getViewDocumentsScenario(CaseData caseData) {
        return nonNull(caseData.getCaseDocumentUploadDateRes()) || nonNull(caseData.getCaseDocumentUploadDate())
            ? SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_AVAILABLE_DEFENDANT
            : SCENARIO_AAA6_CP_STAY_LIFTED_VIEW_DOCUMENTS_TASK_NOT_AVAILABLE_DEFENDANT;
    }

}
