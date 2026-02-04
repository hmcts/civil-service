package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createlipclaim;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@Service
public class CreateLipClaimDashboardService extends DashboardScenarioService {

    public CreateLipClaimDashboardService(DashboardScenariosService dashboardScenariosService,
                                          DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCreateLipClaim(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return isHelpWithFeesCase(caseData)
            ? SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED.getScenario()
            : SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_FEE_REQUIRED.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (!caseData.isApplicantNotRepresented()) {
            return Map.of();
        }
        return Map.of(
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(), true,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(), true
        );
    }

    @Override
    protected String getExtraScenario() {
        return SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented() && isFastTrack(caseData);
    }

    private boolean isFastTrack(CaseData caseData) {
        AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null);
        return FAST_CLAIM.equals(allocatedTrack);
    }

    private boolean isHelpWithFeesCase(CaseData caseData) {
        return caseData.isHelpWithFees();
    }
}
