package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_CLAIMANT;

import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import org.springframework.stereotype.Service;

@Service
public class TrialReadyNotificationClaimantDashboardService extends DashboardScenarioService {

    public TrialReadyNotificationClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                          DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);

    }

    public void notifyTrialReadyNotification(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented() && SdoHelper.isFastTrack(caseData)
            && isNull(caseData.getTrialReadyApplicant());
    }
}
