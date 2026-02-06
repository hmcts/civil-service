package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT;

@Service
public class ApplicationIssuedApplicantDashboardService extends GaDashboardScenarioService {

    private final GeneralAppFeesService generalAppFeesService;

    public ApplicationIssuedApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                                      GaDashboardNotificationsParamsMapper mapper,
                                                      GeneralAppFeesService generalAppFeesService) {
        super(dashboardApiClient, mapper);
        this.generalAppFeesService = generalAppFeesService;
    }

    public void notifyApplicationIssued(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(GeneralApplicationCaseData caseData) {
        return generalAppFeesService.isFreeApplication(caseData)
            ? SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario()
            : SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT.getScenario();
    }
}
