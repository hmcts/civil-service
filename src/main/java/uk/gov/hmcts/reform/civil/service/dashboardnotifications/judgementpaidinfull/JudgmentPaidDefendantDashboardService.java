package uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MARK_PAID_IN_FULL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.MarkPaidInFullUtil.checkMarkPaidInFullAndPaidForApplication;

@Slf4j
@Service
public class JudgmentPaidDefendantDashboardService extends DashboardScenarioService {

    public JudgmentPaidDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyJudgmentPaidInFull(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (checkMarkPaidInFullAndPaidForApplication(caseData)) {
            log.info("JudgmentPaidDefendantDashboardService is called {}", caseData.getCcdCaseReference());
            return SCENARIO_AAA6_MARK_PAID_IN_FULL_DEFENDANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }
}
