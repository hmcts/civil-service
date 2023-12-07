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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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

        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private boolean isJudicialReferralAllowed(CaseData caseData) {
        return (caseData.isClaimantNotSettlePartAdmitClaim() || caseData.isFullDefence() || caseData.isFullDefenceNotPaid())
            && ((Objects.nonNull(caseData.getCaseDataLiP()) && caseData.getCaseDataLiP().hasClaimantNotAgreedToFreeMediation())
            || caseData.isFastTrackClaim());
    }

    private String setUpCaseState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (isJudicialReferralAllowed(updatedData)) {
            return CaseState.JUDICIAL_REFERRAL.name();
        } else if (updatedData.hasDefendantAgreedToFreeMediation() && updatedData.hasClaimantAgreedToFreeMediation()) {
            return CaseState.IN_MEDIATION.name();
        } else if (updatedData.hasApplicant1SignedSettlementAgreement() && updatedData.hasApplicantAcceptedRepaymentPlan()) {
            return CaseState.All_FINAL_ORDERS_ISSUED.name();
        } else if (Objects.nonNull(updatedData.getApplicant1PartAdmitIntentionToSettleClaimSpec()) && updatedData.isClaimantIntentionSettlePartAdmit()) {
            return CaseState.CASE_SETTLED.name();
        } else if (updatedData.hasApplicantNotProceededWithClaim()) {
            return CaseState.CASE_DISMISSED.name();
        } else if (updatedData.hasApplicantRejectedRepaymentPlan()
            && updatedData.getRespondent1().isCompanyOROrganisation()) {
            return CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name();
        } else {
            return response.build().getState();
        }
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        response.state(setUpCaseState(response, updatedData));
    }
}
