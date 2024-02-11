package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;

@Service
@RequiredArgsConstructor
public class PartialRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(PARTIAL_REMISSION_HWF_GRANTED);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
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
        var remissionAmount = caseData.getHwFeesDetails().getRemissionAmount();
        var claimFeeAmount = getClaimFeeAmount(caseData);
        var hearingFeeAmount = getHearingFeeAmount(caseData);
        var feeType = getHwfFeeType(caseData);
        List<String> errors = new ArrayList<>();

        if (FeeType.CLAIMISSUED == feeType && remissionAmount.compareTo(claimFeeAmount) > 0) {
            errors.add("Remission amount should be less than or equal to hearing fee");
        } else if (FeeType.HEARING == feeType && remissionAmount.compareTo(hearingFeeAmount) > 0) {
            errors.add("Remission amount should be less than or equal to hearing fee");
        } else {
            errors.add("Fee Type is not configured properly.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse partRemissionHWF(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var updatedData = caseData.toBuilder();
        var remissionAmount = caseData.getHwFeesDetails().getRemissionAmount();
        var claimFeeAmount = getClaimFeeAmount(caseData);
        var hearingFeeAmount = getHearingFeeAmount(caseData);
        var feeType = getHwfFeeType(caseData);

        if (FeeType.CLAIMISSUED == feeType && claimFeeAmount != null) {
            var updatedClaimFeeAmount = claimFeeAmount.subtract(remissionAmount);
            var claimFee = caseData.getClaimFee();

            claimFee.setCalculatedAmountInPence(updatedClaimFeeAmount);
            updatedData.claimFee(claimFee);
        } else if (FeeType.HEARING == feeType && hearingFeeAmount != null) {
            var updatedHearingFeeAmount = hearingFeeAmount.subtract(remissionAmount);
            var hearingFee = caseData.getHearingFee();

            hearingFee.setCalculatedAmountInPence(updatedHearingFeeAmount);
            updatedData.hearingFee(hearingFee);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private FeeType getHwfFeeType(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getHwFeesDetails)
            .map(HelpWithFeesDetails::getHwfFeeType)
            .orElse(null);
    }

    private BigDecimal getClaimFeeAmount(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getClaimFee)
            .map(Fee::getCalculatedAmountInPence)
            .orElse(null);
    }

    private BigDecimal getHearingFeeAmount(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getHearingFee)
            .map(Fee::getCalculatedAmountInPence)
            .orElse(null);
    }
}
