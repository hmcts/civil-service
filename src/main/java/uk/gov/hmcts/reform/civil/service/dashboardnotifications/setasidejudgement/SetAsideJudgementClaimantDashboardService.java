package uk.gov.hmcts.reform.civil.service.dashboardnotifications.setasidejudgement;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_SET_ASIDE_ERROR_CLAIMANT;

@Service
public class SetAsideJudgementClaimantDashboardService extends DashboardScenarioService {

    public SetAsideJudgementClaimantDashboardService(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifySetAsideJudgement(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.getJoSetAsideReason() == JudgmentSetAsideReason.JUDGMENT_ERROR) {
            return SCENARIO_AAA6_JUDGEMENTS_ONLINE_SET_ASIDE_ERROR_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }
}
