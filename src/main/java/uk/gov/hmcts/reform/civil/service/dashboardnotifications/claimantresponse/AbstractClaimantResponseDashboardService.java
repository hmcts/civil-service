package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Optional;

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

    protected String resolveMintiScenario(CaseData caseData, String scenario) {
        if (scenario == null) {
            return null;
        }
        return isMintiApplicable(caseData) && isCaseStateAwaitingApplicantIntention(caseData) ? scenario : null;
    }

    protected String resolveMediationScenario(CaseData caseData, String carmScenario, String nonCarmScenario) {
        if (caseData.getCcdState() != CaseState.IN_MEDIATION) {
            return null;
        }
        return isCarmApplicableForMediation(caseData) ? carmScenario : nonCarmScenario;
    }

    protected String resolveClaimantEndsClaimScenario(CaseData caseData, String scenario) {
        if (caseData.getCcdState() == CaseState.CASE_STAYED
            && caseData.isClaimantDontWantToProceedWithFulLDefenceFD()) {
            return scenario;
        }
        return null;
    }

    protected String resolveRejectRepaymentScenario(CaseData caseData,
                                                    boolean includeLrVLip,
                                                    String scenarioNonJo,
                                                    String scenarioJo) {
        if (!isClaimantRejectRepaymentPlan(caseData, includeLrVLip)) {
            return null;
        }
        if (scenarioJo == null) {
            return scenarioNonJo;
        }
        return featureToggleService.isJudgmentOnlineLive() ? scenarioJo : scenarioNonJo;
    }

    protected boolean isClaimantRejectRepaymentPlan(CaseData caseData, boolean includeLrVLip) {
        return (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && (caseData.getRespondent1().isCompanyOROrganisation()
            || (includeLrVLip && caseData.isLRvLipOneVOne()))
            && caseData.hasApplicantRejectedRepaymentPlan();
    }

    protected boolean hasClaimantRejectedCourtDecision(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getCaseDataLiP)
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
            .isPresent();
    }

    protected abstract String citizenRole();

    protected abstract String generalApplicationInactiveScenario();
}
