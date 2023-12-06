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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final ObjectMapper objectMapper;
    private final Time time;
    private final UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService;

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
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                .applicant1ResponseDate(LocalDateTime.now())
                .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI));

        updateCaseManagementLocationDetailsService.updateCaseManagementDetails(builder, callbackParams);

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));

        updateClaimStateJudicialReferral(response, updatedData);
        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private void updateClaimStateJudicialReferral(
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response,
        CaseData caseData) {
        if (isJudicialReferralAllowed(caseData)) {
            response.state(CaseState.JUDICIAL_REFERRAL.name());
        }
    }

    private boolean isJudicialReferralAllowed(CaseData caseData) {
        return (caseData.isClaimantNotSettlePartAdmitClaim() || caseData.isFullDefence())
            && (Objects.nonNull(caseData.getCaseDataLiP()) && caseData.getCaseDataLiP().hasClaimantNotAgreedToFreeMediation());
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (updatedData.hasDefendantAgreedToFreeMediation() && updatedData.hasClaimantAgreedToFreeMediation()) {
            response.state(CaseState.IN_MEDIATION.name());
        } else if (updatedData.hasApplicant1SignedSettlementAgreement() && updatedData.hasApplicantAcceptedRepaymentPlan()) {
            response.state(CaseState.All_FINAL_ORDERS_ISSUED.name());
        } else if (isCaseSettledAllowed(updatedData)){
            response.state(CaseState.CASE_SETTLED.name());
        } else if (updatedData.hasApplicantNotProceededWithClaim()) {
            response.state(CaseState.CASE_DISMISSED.name());
        } else if (updatedData.hasApplicantRejectedRepaymentPlan()
            && updatedData.getRespondent1().isCompanyOROrganisation()) {
            response.state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }
    }

    private boolean isCaseSettledAllowed(CaseData caseData) {
        return ((Objects.nonNull(caseData.getApplicant1PartAdmitIntentionToSettleClaimSpec()) && caseData.isClaimantIntentionSettlePartAdmit()) ||
                (caseData.isPartAdmitImmediatePaymentClaimSettled()));
    }
}
