package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_CLAIMANT;

@Service
public class InitiateCoscClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;
    private final CoscDashboardHelper coscDashboardHelper;

    protected InitiateCoscClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationsParamsMapper mapper,
                                                   FeatureToggleService featureToggleService,
                                                   CoscDashboardHelper coscDashboardHelper) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.coscDashboardHelper = coscDashboardHelper;
    }

    public void notifyInitiateCosc(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && !coscDashboardHelper.isMarkedPaidInFull(caseData)
            && YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }
}
