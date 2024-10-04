package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.utils.CaseStateUtils.shouldMoveToInMediationState;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetermineNextState  {

    private final FeatureToggleService featureToggleService;
    private final JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;

    public String determineNextState(CaseData caseData,
                                     CallbackParams callbackParams,
                                     CaseData.CaseDataBuilder<?, ?> builder,
                                     String nextState,
                                     BusinessProcess businessProcess) {

        log.info("Determining next state for Case : {}", caseData.getCcdCaseReference());
        if (V_2.equals(callbackParams.getVersion())
            && featureToggleService.isPinInPostEnabled()
            && isOneVOne(caseData)) {

            log.debug("Pin in Post enabled for Case : {}", caseData.getCcdCaseReference());
            if (caseData.hasClaimantAgreedToFreeMediation()) {
                nextState = CaseState.IN_MEDIATION.name();
            } else if (caseData.hasApplicantAcceptedRepaymentPlan()) {
                Pair<String, BusinessProcess> result = handleAcceptedRepaymentPlan(caseData, builder, businessProcess);
                nextState = result.getLeft();
                businessProcess = result.getRight();
            } else if (caseData.hasApplicantRejectedRepaymentPlan()) {
                nextState = CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
            } else if (isClaimNotSettled(caseData)) {
                nextState = CaseState.JUDICIAL_REFERRAL.name();
            } else if (caseData.isPartAdmitClaimSettled()) {
                nextState = CaseState.CASE_SETTLED.name();
            } else if (isLipVLipOneVOne(caseData)) {
                nextState = CaseState.CASE_STAYED.name();
            }
        }

        if (shouldMoveToInMediationState(
            caseData, featureToggleService.isCarmEnabledForCase(caseData))) {
            nextState = CaseState.IN_MEDIATION.name();
            businessProcess = BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC);
        }

        builder.businessProcess(businessProcess);
        return nextState;
    }

    private static boolean isClaimNotSettled(CaseData caseData) {
        return caseData.isClaimantNotSettlePartAdmitClaim()
            && ((caseData.hasClaimantNotAgreedToFreeMediation()
            || caseData.hasDefendantNotAgreedToFreeMediation())
            || caseData.isFastTrackClaim());
    }

    private boolean isLipVLipOneVOne(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && caseData.isLRvLipOneVOne()
            && caseData.isClaimantDontWantToProceedWithFulLDefenceFD();
    }

    private Pair<String, BusinessProcess> handleAcceptedRepaymentPlan(CaseData caseData,
                                               CaseData.CaseDataBuilder<?, ?> builder,
                                               BusinessProcess businessProcess) {
        String nextState;
        if (featureToggleService.isJudgmentOnlineLive()
            && (caseData.isPayByInstallment() || caseData.isPayBySetDate())
            && caseData.isLRvLipOneVOne()) {
            nextState = CaseState.All_FINAL_ORDERS_ISSUED.name();
            businessProcess = BusinessProcess.ready(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC);

        } else {
            nextState = CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        }
        if (featureToggleService.isJudgmentOnlineLive()) {
            JudgmentDetails activeJudgment = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);
            builder.activeJudgment(activeJudgment);
            builder.joIsLiveJudgmentExists(YesOrNo.YES);
            builder.joRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummary(activeJudgment));
        }

        return Pair.of(nextState, businessProcess);
    }
}