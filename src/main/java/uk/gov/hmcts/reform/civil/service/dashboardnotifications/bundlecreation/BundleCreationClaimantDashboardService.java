package uk.gov.hmcts.reform.civil.service.dashboardnotifications.bundlecreation;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

@Service
public class BundleCreationClaimantDashboardService extends DashboardScenarioService {

    public BundleCreationClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                  DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyBundleCreated(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_BUNDLE_CREATED_TRIAL_READY_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }
}
