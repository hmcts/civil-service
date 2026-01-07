package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
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
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT;

@Service
public class ClaimantResponseClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final TaskListService taskListService;
    private final FeatureToggleService featureToggleService;

    public ClaimantResponseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
        this.taskListService = taskListService;
    }

    public void notifyClaimantResponse(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (isMintiApplicable(caseData) && isCaseStateAwaitingApplicantIntention(caseData)) {
            return SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario();
        } else if (caseData.getCcdState() == CASE_SETTLED) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario();
        } else if (caseData.getCcdState() == JUDICIAL_REFERRAL) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario();
        } else if (caseData.getCcdState() == IN_MEDIATION) {
            if (isCarmApplicableForMediation(caseData)) {
                return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario();
            }
            return SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario();
        } else if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario();
        } else if (hasClaimantRejectedCourtDecision(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario();
        } else if (caseData.getCcdState() == CASE_STAYED && caseData.isClaimantDontWantToProceedWithFulLDefenceFD()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario();
        }

        if (caseData.isPayBySetDate() || caseData.isPayByInstallment()) {
            if (caseData.getRespondent1().isCompanyOROrganisation() && caseData.hasApplicantRejectedRepaymentPlan()) {
                return featureToggleService.isJudgmentOnlineLive()
                    ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
                    : SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario();
            }
        }

        if (caseData.isPartAdmitImmediatePaymentClaimSettled()) {
            return SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        String caseId = String.valueOf(caseData.getCcdCaseReference());
        if (caseData.getCcdState() == CASE_SETTLED) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, CLAIMANT_ROLE);
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseId, CLAIMANT_ROLE);
        }

        if (caseData.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM) {
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData))
                .build();

            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(),
                caseId,
                scenarioParams
            );
        }
    }

    private boolean hasClaimantRejectedCourtDecision(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getCaseDataLiP)
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
            .isPresent();
    }

    private boolean isMintiApplicable(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    private static boolean isCaseStateAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState() == AWAITING_APPLICANT_INTENTION;
    }

    private boolean isCarmApplicableForMediation(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData);
    }
}
