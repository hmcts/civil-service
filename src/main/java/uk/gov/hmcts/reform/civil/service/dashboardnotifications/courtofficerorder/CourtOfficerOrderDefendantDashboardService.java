package uk.gov.hmcts.reform.civil.service.dashboardnotifications.courtofficerorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Objects;

@Service
public class CourtOfficerOrderDefendantDashboardService extends DashboardScenarioService {

    public CourtOfficerOrderDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                      DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCourtOfficerOrder(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario();
    }

    @Override
    public String getExtraScenario() {
        return DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    public boolean shouldRecordExtraScenario(CaseData caseData) {
        return caseData.isRespondent1LiP()
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack())
            && Objects.isNull(caseData.getTrialReadyRespondent1());
    }
}
