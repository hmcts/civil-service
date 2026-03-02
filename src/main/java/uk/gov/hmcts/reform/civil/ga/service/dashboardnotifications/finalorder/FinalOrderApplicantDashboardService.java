package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT;

@Service
public class FinalOrderApplicantDashboardService extends GaDashboardScenarioService {

    public FinalOrderApplicantDashboardService(DashboardApiClient dashboardApiClient,
                                               GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyFinalOrder(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
        return Objects.nonNull(caseData.getIsGaApplicantLip())
            && caseData.getIsGaApplicantLip().equals(YES);
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        return SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT.getScenario();
    }
}
