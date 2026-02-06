package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.hwf;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT;

@Service
public class HwfOutcomeApplicantDashboardService extends GaDashboardScenarioService {

    private static final Map<CaseEvent, String> DASHBOARD_SCENARIOS = Map.of(
        NO_REMISSION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario(),
        MORE_INFORMATION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT.getScenario(),
        PARTIAL_REMISSION_HWF_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT.getScenario(),
        INVALID_HWF_REFERENCE_GA, SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario()
    );

    public HwfOutcomeApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                               GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyHwfOutcome(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {

        return switch(caseData.getHwfFeeType()) {
            case APPLICATION -> DASHBOARD_SCENARIOS.get(caseData.getGaHwfDetails().getHwfCaseEvent());
            case ADDITIONAL -> DASHBOARD_SCENARIOS.get(caseData.getAdditionalHwfDetails().getHwfCaseEvent());
            default -> "";
        };
    }
}
