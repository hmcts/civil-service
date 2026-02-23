package uk.gov.hmcts.reform.civil.service.dashboardnotifications.helpwithhearingfees;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import org.springframework.stereotype.Service;

@Service
public class HelpWithHearingFeesClaimantDashboardService extends DashboardScenarioService {

    public HelpWithHearingFeesClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyHelpWithHearingFees(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }
}
