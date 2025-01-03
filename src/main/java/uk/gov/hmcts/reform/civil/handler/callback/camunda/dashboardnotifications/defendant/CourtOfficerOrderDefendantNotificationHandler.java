package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_DEFENDANT;

@Service
public class CourtOfficerOrderDefendantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_DEFENDANT);

    public static final String TASK_ID = "GenerateDefendantDashboardNotificationCourtOfficerOrder";

    public CourtOfficerOrderDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                         DashboardNotificationsParamsMapper mapper,
                                                         FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario();
    }

    @Override
    public String getExtraScenario() {
        return SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_DEFENDANT.getScenario();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isCaseEventsEnabled() && caseData.isRespondent1LiP();
    }

    @Override
    public boolean shouldRecordExtraScenario(CaseData caseData) {
        return featureToggleService.isCaseEventsEnabled()
            && caseData.isRespondent1LiP()
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack())
            && Objects.isNull(caseData.getTrialReadyRespondent1());
    }
}
