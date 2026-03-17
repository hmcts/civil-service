package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@Service
public class CreateClaimAfterPaymentApplicantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public CreateClaimAfterPaymentApplicantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                           DashboardNotificationsParamsMapper mapper,
                                                           FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (!isEligible(caseData)) {
            return Map.of();
        }

        Map<String, Boolean> scenarios = new HashMap<>();
        scenarios.put(SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
                      featureToggleService.isLipQueryManagementEnabled(caseData));
        scenarios.put(SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
                      featureToggleService.isLipQueryManagementEnabled(caseData));
        scenarios.put(SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT.getScenario(),
                      caseData.isHWFTypeClaimIssued() && caseData.claimIssueFullRemissionNotGrantedHWF());
        return scenarios;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return isEligible(caseData);
    }

    private boolean isEligible(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && featureToggleService.isDashboardEnabledForCase(caseData)
            && LipPredicate.caseContainsLiP.test(caseData)
            && caseData.isApplicant1NotRepresented();
    }
}
