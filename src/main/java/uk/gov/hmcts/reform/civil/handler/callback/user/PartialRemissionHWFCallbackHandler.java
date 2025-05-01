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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartialRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(PARTIAL_REMISSION_HWF_GRANTED);
    public static final String ERR_MSG_FEE_TYPE_NOT_CONFIGURED = "Fee Type is not configured properly";
    public static final String ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE = "Remission amount must be less than claim fee";
    public static final String ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE = "Remission amount must be less than hearing fee";
    public static final String ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO = "Remission amount must be greater than zero";

    private final ObjectMapper objectMapper;
    private final HWFFeePaymentOutcomeService hwfFeePaymentOutcomeService;

    private final Map<String, Callback> callbackMap = java.util.Map.of(
        callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
        callbackKey(MID, "remission-amount"), this::validateRemissionAmount,
        callbackKey(ABOUT_TO_SUBMIT),
        this::partRemissionHWF,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateRemissionAmount(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var claimIssuedRemissionAmount = caseData.getClaimIssueRemissionAmount();
        var hearingRemissionAmount = caseData.getHearingRemissionAmount();
        var claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        var hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();
        var feeType = caseData.getHwfFeeType();
        var errors = new ArrayList<String>();

        if (feeType == null) {
            errors.add(ERR_MSG_FEE_TYPE_NOT_CONFIGURED);
        }

        if (claimIssuedRemissionAmount.signum() == -1 || hearingRemissionAmount.signum() == -1) {
            errors.add(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_ZERO);
        } else if (caseData.isHWFTypeClaimIssued() && claimIssuedRemissionAmount.compareTo(claimFeeAmount) >= 0) {
            errors.add(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_CLAIM_FEE);
        } else if (caseData.isHWFTypeHearing() && hearingRemissionAmount.compareTo(hearingFeeAmount) >= 0) {
            errors.add(ERR_MSG_REMISSION_AMOUNT_LESS_THAN_HEARING_FEE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse partRemissionHWF(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = hwfFeePaymentOutcomeService.updateOutstandingFee(caseData, callbackParams.getRequest().getEventId());
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME));

        if (caseData.isHWFTypeHearing()) {
            HelpWithFeesDetails hearingFeeDetails = caseData.getHearingHwfDetails();
            updatedData.hearingHwfDetails(hearingFeeDetails.toBuilder().hwfCaseEvent(PARTIAL_REMISSION_HWF_GRANTED).build());
        }
        if (caseData.isHWFTypeClaimIssued()) {
            updatedData.claimIssuedHwfDetails(caseData.getClaimIssuedHwfDetails().toBuilder().hwfCaseEvent(
                PARTIAL_REMISSION_HWF_GRANTED).build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
