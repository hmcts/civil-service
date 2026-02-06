package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT;

@Service
public class ApplicationSubmittedApplicantDashboardService extends GaDashboardScenarioService {

    public ApplicationSubmittedApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                                         GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyApplicationSubmitted(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(GeneralApplicationCaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();
        if (Objects.nonNull(caseData.getGaHwfDetails())) {
            if (caseData.gaApplicationFeeFullRemissionNotGrantedHWF(caseData)) {
                scenarios.put(SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT.getScenario(), true);
            } else {
                scenarios.put(SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT.getScenario(), true);
            }
        }
        return scenarios;
    }
}
