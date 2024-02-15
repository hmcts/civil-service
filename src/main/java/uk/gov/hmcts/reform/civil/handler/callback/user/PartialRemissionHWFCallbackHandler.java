package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
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

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::partRemissionHWF)
            .put(callbackKey(MID, "remission-amount"), this::validateRemissionAmount)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
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
        var hearingFeeAmount = caseData.getHearingFeeAmount();
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

        caseData = calculateOutstandingFee(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CaseData calculateOutstandingFee(CaseData caseData) {

        BigDecimal claimIssuedRemissionAmount = caseData.getClaimIssueRemissionAmount();
        BigDecimal hearingRemissionAmount = caseData.getHearingRemissionAmount();
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getHearingFeeAmount();
        BigDecimal outstandingFeeAmount;

        if (caseData.isHWFTypeClaimIssued() && BigDecimal.ZERO.compareTo(claimFeeAmount) != 0) {
            outstandingFeeAmount = claimFeeAmount.subtract(claimIssuedRemissionAmount);
            caseData.getClaimIssuedHwfDetails().setOutstandingFee(outstandingFeeAmount);
            caseData.getClaimIssuedHwfDetails().setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount));
        } else if (caseData.isHWFTypeHearing() && BigDecimal.ZERO.compareTo(hearingFeeAmount) != 0) {
            outstandingFeeAmount = hearingFeeAmount.subtract(hearingRemissionAmount);
            caseData.getHearingHwfDetails().setOutstandingFee(outstandingFeeAmount);
            caseData.getHearingHwfDetails().setOutstandingFeeInPounds(MonetaryConversions.penniesToPounds(outstandingFeeAmount));
        }
        return caseData;
    }
}
