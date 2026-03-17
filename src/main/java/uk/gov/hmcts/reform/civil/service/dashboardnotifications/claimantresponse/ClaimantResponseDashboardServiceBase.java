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

import java.util.function.Supplier;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;

abstract class ClaimantResponseDashboardServiceBase extends DashboardScenarioService {

    protected final FeatureToggleService featureToggleService;
    protected final DashboardNotificationService dashboardNotificationService;
    protected final TaskListService taskListService;

    protected ClaimantResponseDashboardServiceBase(DashboardScenariosService dashboardScenariosService,
                                                   DashboardNotificationsParamsMapper mapper,
                                                   FeatureToggleService featureToggleService,
                                                   DashboardNotificationService dashboardNotificationService,
                                                   TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    protected boolean isMintiApplicable(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    protected boolean isCarmApplicableForMediation(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData);
    }

    protected static boolean isCaseStateAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState() == AWAITING_APPLICANT_INTENTION;
    }

    protected static boolean isCaseStateSettled(CaseData caseData) {
        return caseData.getCcdState() == CASE_SETTLED;
    }

    protected static boolean isCaseStateJudicialReferral(CaseData caseData) {
        return caseData.getCcdState() == JUDICIAL_REFERRAL;
    }

    protected static boolean isCaseStateInMediation(CaseData caseData) {
        return caseData.getCcdState() == IN_MEDIATION;
    }

    protected void clearSettledCaseNotificationsIfNeeded(CaseData caseData, String role) {
        if (!isCaseStateSettled(caseData)) {
            return;
        }
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, role);
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseId, role);
    }

    protected void recordGeneralApplicationScenarioIfNeeded(CaseData caseData, String authToken, String scenario) {
        if (caseData.getCcdState() != CaseState.PROCEEDS_IN_HERITAGE_SYSTEM) {
            return;
        }
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        ScenarioRequestParams notificationParams = ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
        dashboardScenariosService.recordScenarios(
            authToken,
            scenario,
            caseId,
            notificationParams
        );
    }

    @SafeVarargs
    protected final String resolveScenario(Supplier<String>... scenarioResolvers) {
        for (Supplier<String> resolver : scenarioResolvers) {
            String scenario = resolver.get();
            if (scenario != null) {
                return scenario;
            }
        }
        return null;
    }
}
