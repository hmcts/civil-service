package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.Optional;

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
public class ClaimantResponseClaimantDashboardService extends ClaimantResponseDashboardServiceBase {

    public ClaimantResponseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService,
                                                    DashboardNotificationService dashboardNotificationService,
                                                    TaskListService taskListService) {
        super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (isMintiApplicable(caseData) && isCaseStateAwaitingApplicantIntention(caseData)) {
            return SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario();
        } else if (isCaseStateSettled(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario();
        } else if (isCaseStateJudicialReferral(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario();
        } else if (isCaseStateInMediation(caseData)) {
            if (isCarmApplicableForMediation(caseData)) {
                return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario();
            } else {
                return SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario();
            }
        } else if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario();
        } else if (hasClaimantRejectedCourtDecision(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario();
        } else if (caseData.getCcdState() == CaseState.CASE_STAYED && caseData.isClaimantDontWantToProceedWithFulLDefenceFD()) {
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
        return featureToggleService.isLipVLipEnabled() && caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        clearSettledCaseNotificationsIfNeeded(caseData, CLAIMANT_ROLE);
        recordGeneralApplicationScenarioIfNeeded(
            caseData,
            authToken,
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()
        );
    }

    private boolean hasClaimantRejectedCourtDecision(CaseData caseData) {
        return
            Optional.ofNullable(caseData)
                .map(CaseData::getCaseDataLiP)
                .map(CaseDataLiP::getApplicant1LiPResponse)
                .filter(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
                .isPresent();
    }
}
