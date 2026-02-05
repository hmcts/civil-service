package uk.gov.hmcts.reform.civil.service.dashboardnotifications.djnondivergent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

@Service
public class DjNonDivergentClaimantDashboardService extends DashboardScenarioService {

    public DjNonDivergentClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyDjNonDivergent(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_JUDGMENT_ONLINE_DEFAULT_JUDGMENT_ISSUED_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantLiP();
    }
}
