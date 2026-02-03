package uk.gov.hmcts.reform.civil.service.dashboardnotifications.recordjudgment;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

@Service
public class RecordJudgmentDefendantDashboardService extends DashboardScenarioService {

    public RecordJudgmentDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);

    }

    public void notifyRecordJudgment(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.getJoPaymentPlan().getType().equals(PaymentPlanSelection.PAY_IN_INSTALMENTS);
    }
}
