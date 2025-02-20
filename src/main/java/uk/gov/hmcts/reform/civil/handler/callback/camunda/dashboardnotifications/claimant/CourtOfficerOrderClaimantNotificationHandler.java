package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_HEARING_FEE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_CLAIMANT;

@Service
public class CourtOfficerOrderClaimantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT);

    public static final String TASK_ID = "GenerateClaimantDashboardNotificationCourtOfficerOrder";

    public CourtOfficerOrderClaimantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return caseData.isHearingFeePaid()
            ? SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_HEARING_FEE_CLAIMANT.getScenario();
    }

    @Override
    public String getExtraScenario() {
        return SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_CLAIMANT.getScenario();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isCaseEventsEnabled() && caseData.isApplicantLiP();
    }

    @Override
    public boolean shouldRecordExtraScenario(CaseData caseData) {
        return featureToggleService.isCaseEventsEnabled()
            && caseData.isApplicantLiP()
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack())
            && Objects.isNull(caseData.getTrialReadyApplicant());
    }
}
