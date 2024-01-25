package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_CUI);

    private final ResponseOneVOneShowTagService responseOneVOneService;
    private final FeatureToggleService featureToggleService;
    private final JudgementService judgementService;

    private final ObjectMapper objectMapper;
    private final Time time;
    private final UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateCaseData,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updatedCaseData.showResponseOneVOneFlag(responseOneVOneService.setUpOneVOneFlow(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime applicant1ResponseDate = LocalDateTime.now();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .applicant1ResponseDate(applicant1ResponseDate)
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI))
            .respondent1RespondToSettlementAgreementDeadline(caseData.isBilingual() ? null : getRespondToSettlementAgreementDeadline(
                caseData,
                applicant1ResponseDate
            ));

        updateCaseManagementLocationDetailsService.updateCaseManagementDetails(builder, callbackParams);

        if (caseData.hasClaimantAgreedToFreeMediation() && caseData.hasDefendantAgreedToFreeMediation()) {
            builder.claimMovedToMediationOn(LocalDate.now());
        }
        updateCcjRequestPaymentDetails(builder, caseData);

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));

        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private LocalDateTime getRespondToSettlementAgreementDeadline(CaseData caseData, LocalDateTime responseDate) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            ? deadlinesCalculator.getRespondToSettlementAgreementDeadline(responseDate) : null;
    }

    private boolean isProceedsInHeritageSystemAllowed(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        boolean isCourtDecisionAccepted = applicant1Response != null
            && applicant1Response.hasClaimantAcceptedCourtDecision();
        boolean isCourtDecisionRejected = applicant1Response != null
            && applicant1Response.hasClaimantRejectedCourtDecision();
        boolean isCcjRequested = applicant1Response != null
            && applicant1Response.hasApplicant1RequestedCcj();
        boolean isInFavourOfClaimant = applicant1Response != null
            && applicant1Response.hasCourtDecisionInFavourOfClaimant();

        return (caseData.hasApplicantRejectedRepaymentPlan()
            && caseData.getRespondent1().isCompanyOROrganisation())
            || ((caseData.hasApplicantAcceptedRepaymentPlan()
            || isCourtDecisionAccepted
            || isInFavourOfClaimant)
            && isCcjRequested)
            || isCourtDecisionRejected;
    }

    private String setUpCaseState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (isJudicialReferralAllowed(updatedData)) {
            return CaseState.JUDICIAL_REFERRAL.name();
        } else if (updatedData.hasDefendantAgreedToFreeMediation() && updatedData.hasClaimantAgreedToFreeMediation()) {
            return CaseState.IN_MEDIATION.name();
        } else if (isAllFinalOrderIssued(updatedData)) {
            return CaseState.All_FINAL_ORDERS_ISSUED.name();
        } else if (isCaseSettledAllowed(updatedData)) {
            return CaseState.CASE_SETTLED.name();
        } else if (updatedData.hasApplicantNotProceededWithClaim()) {
            return CaseState.CASE_DISMISSED.name();
        } else if (isProceedsInHeritageSystemAllowed(updatedData)) {
            return CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        } else {
            return response.build().getState();
        }
    }

    private boolean isCaseSettledAllowed(CaseData caseData) {
        return ((Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec())
            && caseData.isClaimantIntentionSettlePartAdmit())
            || (caseData.isPartAdmitImmediatePaymentClaimSettled()));
    }

    private void updateCcjRequestPaymentDetails(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        if (hasCcjRequest(caseData)) {
            CCJPaymentDetails ccjPaymentDetails = judgementService.buildJudgmentAmountSummaryDetails(caseData);
            builder.ccjPaymentDetails(ccjPaymentDetails).build();
        }
    }

    private boolean hasCcjRequest(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
            && caseData.hasApplicant1AcceptedCcj() && caseData.isCcjRequestJudgmentByAdmission());
    }

    private boolean isJudicialReferralAllowed(CaseData caseData) {
        return isProceedOrNotSettleClaim(caseData)
            && (isClaimantOrDefendantRejectMediation(caseData)
            || caseData.isFastTrackClaim());
    }

    private boolean isProceedOrNotSettleClaim(CaseData caseData) {
        return caseData.isClaimantNotSettlePartAdmitClaim() || caseData.isFullDefence() || caseData.isFullDefenceNotPaid();
    }

    private boolean isClaimantOrDefendantRejectMediation(CaseData caseData) {
        return (Objects.nonNull(caseData.getCaseDataLiP()) && caseData.getCaseDataLiP().hasClaimantNotAgreedToFreeMediation())
            || caseData.hasDefendantNotAgreedToFreeMediation();
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        response.state(setUpCaseState(response, updatedData));
    }

    private boolean isAllFinalOrderIssued(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        boolean isCourtDecisionAccepted = applicant1Response != null
            && applicant1Response.hasClaimantAcceptedCourtDecision();
        boolean isInFavourOfClaimant = applicant1Response != null
            && applicant1Response.hasCourtDecisionInFavourOfClaimant();

        return (caseData.hasApplicantRejectedRepaymentPlan()
            && (isCourtDecisionAccepted || isInFavourOfClaimant))
            || caseData.hasApplicantAcceptedRepaymentPlan()
            && caseData.hasApplicant1SignedSettlementAgreement();
    }
}
