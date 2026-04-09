package uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MARK_PAID_IN_FULL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.MarkPaidInFullUtil.checkMarkPaidInFull;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JudgmentPaidClaimantDashboardService extends DashboardScenarioService {

    public JudgmentPaidClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                 DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyJudgmentPaidInFull(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (checkMarkPaidInFull(caseData)) {
            log.info("JudgmentPaidDefendantDashboardService is called {}", caseData.getCcdCaseReference());
            return SCENARIO_AAA6_MARK_PAID_IN_FULL_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_CONFIRMATION_JUDGMENT_PAID_IN_FULL_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }
}
