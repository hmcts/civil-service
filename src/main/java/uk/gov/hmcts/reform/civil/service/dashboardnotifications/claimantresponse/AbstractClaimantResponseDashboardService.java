package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

public abstract class AbstractClaimantResponseDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    protected final FeatureToggleService featureToggleService;

    protected AbstractClaimantResponseDashboardService(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper,
                                                       FeatureToggleService featureToggleService,
                                                       DashboardNotificationService dashboardNotificationService,
                                                       TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimantResponse(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        if (caseData.getCcdState() == CASE_SETTLED) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, citizenRole());
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseId, citizenRole());
        }

        if (caseData.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM) {
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData))
                .build();
            dashboardScenariosService.recordScenarios(
                authToken,
                generalApplicationInactiveScenario(),
                caseId,
                scenarioParams
            );
        }
    }

    protected static boolean isCaseStateAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState() == AWAITING_APPLICANT_INTENTION;
    }

    protected boolean isCarmApplicableForMediation(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData);
    }

    protected boolean isMintiApplicable(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    protected abstract String citizenRole();

    protected abstract String generalApplicationInactiveScenario();
}
