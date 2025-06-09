package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowStateAllowedEventService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIM_STATE_AFTER_CLAIMANT_INTENTION_LR_DOC_UPLOADED;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_PROCEED;
import static uk.gov.hmcts.reform.civil.utils.CaseStateUtils.shouldMoveToInMediationState;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetermineNextState extends CallbackHandler {

    private final List<CaseEvent> Events = Collections.singletonList(UPDATE_CLAIM_STATE_AFTER_CLAIMANT_INTENTION_LR_DOC_UPLOADED);
    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateClaimStatePostTranslation);
    private static final String TASK_ID = "UpdateClaimStateAfterClaimantIntentionLrTranslatedDocUploaded";
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;
    private final JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;
    private final FlowStateAllowedEventService flowStateAllowedEventService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Events;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse updateClaimStatePostTranslation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String nextState = determineNextStatePostTranslation(caseData, callbackParams);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(nextState)
            .build();
    }

    public String determineNextStatePostTranslation(CaseData caseData, CallbackParams callbackParams) {

        String nextState = "";
        log.info("Determining next state (post translation) for Case: {}", caseData.getCcdCaseReference());

        if (V_2.equals(callbackParams.getVersion())
            && featureToggleService.isPinInPostEnabled()
            && isOneVOne(caseData)) {
            log.debug("Pin in Post enabled for Case: {}", caseData.getCcdCaseReference());
            if (caseData.hasClaimantAgreedToFreeMediation()) {
                nextState = CaseState.IN_MEDIATION.name();
            } else if (isDefenceAdmitPayImmediately(caseData)) {
                nextState = getNextState(caseData);
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
        }
        if (shouldNotChangeStateMinti(caseData)) {
            nextState = AWAITING_APPLICANT_INTENTION.name();
        }

        return nextState;
    }

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
            } else if (isDefenceAdmitPayImmediately(caseData)) {
                nextState = getNextState(caseData);
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

        if (shouldNotChangeStateMinti(caseData)) {
            nextState = CaseState.AWAITING_APPLICANT_INTENTION.name();
        }

        if (claimantIntentionNeedsTranslation(caseData)) {
            nextState = CaseState.AWAITING_APPLICANT_INTENTION.name();
        }

        builder.businessProcess(businessProcess);
        return nextState;
    }

    private boolean shouldNotChangeStateMinti(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && isMultiOrIntermediateSpecClaim(caseData)
            && isLipCase(caseData)
            && (isClaimNotSettled(caseData)
            || caseData.getApplicant1ProceedWithClaim() == YesOrNo.YES);
    }

    private boolean isMultiOrIntermediateSpecClaim(CaseData caseData) {
        return INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

    private boolean isLipCase(CaseData caseData) {
        return caseData.isApplicantLiP() || caseData.isRespondent1LiP();
    }

    private boolean isDefenceAdmitPayImmediately(CaseData caseData) {
        return featureToggleService.isJudgmentOnlineLive()
            && IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    private String getNextState(CaseData caseData) {
        if ((caseData.isFullAdmitClaimSpec() && caseData.getApplicant1ProceedWithClaim() == null)
            || (caseData.isPartAdmitImmediatePaymentClaimSettled())) {
            return AWAITING_APPLICANT_INTENTION.name();
        }
        return CaseState.All_FINAL_ORDERS_ISSUED.name();
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
            && (caseData.isPayByInstallment() || caseData.isPayBySetDate())) {
            nextState = CaseState.All_FINAL_ORDERS_ISSUED.name();
            businessProcess = BusinessProcess.ready(JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC);
        } else {
            nextState = CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        }
        if (featureToggleService.isJudgmentOnlineLive()) {
            judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData, builder);
        }

        return Pair.of(nextState, businessProcess);
    }

    private boolean claimantIntentionNeedsTranslation(CaseData caseData) {
        if (!featureToggleService.isGaForWelshEnabled()) {
            return false;
        }
        boolean bilingualRequested = caseData.isLRvLipOneVOne()
            && (caseData.isRespondentResponseBilingual()
            || caseData.isLipDefendantSpecifiedBilingualDocuments());

        log.info("PAUSE CLAIM TRANSLATION RESPONDENT WELSH");
        log.info("NEEDS TRANSLATION RESPONDENT WELSH, {}", bilingualRequested);
        log.info("TRANSLATION REQUIRED IN FLOW STATE, {}", translationRequiredInFlowState(caseData));
        return bilingualRequested && translationRequiredInFlowState(caseData);
    }

    public boolean translationRequiredInFlowState(CaseData caseData) {
        FlowState flowState = flowStateAllowedEventService.getFlowState(caseData);
        return flowState.equals(FULL_DEFENCE_PROCEED)
            || flowState.equals(PART_ADMIT_NOT_SETTLED_NO_MEDIATION)
            || flowState.equals(FULL_ADMIT_PROCEED)
            || flowState.equals(PART_ADMIT_PROCEED)
            || flowState.equals(IN_MEDIATION);
    }
}
