package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM;

@Service
@AllArgsConstructor
public class UploadMediationService {
    private final DashboardNotificationsParamsMapper mapper;
    private final DashboardApiClient dashboardApiClient;

    public String[] getScenarios() {
            return new String[]{
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM.getScenario(),
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAMANT_CARM.getScenario()
            };
        }

    public void recordScenarios(String[] scenarios, CaseData caseData, String authToken) {
        for (String scenario : scenarios) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder()
                    .params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
    }
}
