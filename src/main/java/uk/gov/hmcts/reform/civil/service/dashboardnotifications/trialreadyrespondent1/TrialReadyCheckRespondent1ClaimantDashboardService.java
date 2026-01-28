package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT;

@Service
public class TrialReadyCheckRespondent1ClaimantDashboardService extends DashboardScenarioService {

    public TrialReadyCheckRespondent1ClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                              DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);

    }

    public void notifyTrialReadyCheckRespondent1(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented())
            && isNull(caseData.getTrialReadyApplicant())
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack());
    }

}
