package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT;

@Service
public class TrialReadyCheckDefendantDashboardService extends DashboardScenarioService {

    public TrialReadyCheckDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCaseTrialReadyCheck(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented())
            && isNull(caseData.getTrialReadyRespondent1())
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack());
    }
}
