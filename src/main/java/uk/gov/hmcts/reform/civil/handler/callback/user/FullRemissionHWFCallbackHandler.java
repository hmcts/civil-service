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
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;

@Service
@RequiredArgsConstructor
public class FullRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(FULL_REMISSION_HWF);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::fullRemissionHWF,
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

    private CallbackResponse fullRemissionHWF(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var updatedData = caseData.toBuilder();
        var feeType = caseData.getHwfFeeType();
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();
        HelpWithFeesDetails helpWithFeesDetails = caseData.getHwFeesDetails();

        if (FeeType.CLAIMISSUED == feeType && claimFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            var claimFee = caseData.getClaimFee();
            claimFee.setCalculatedAmountInPence(BigDecimal.ZERO);
            helpWithFeesDetails.setRemissionAmount(claimFeeAmount);

            updatedData.claimFee(claimFee);
            updatedData.hwFeesDetails(helpWithFeesDetails);
        } else if (FeeType.HEARING == feeType && hearingFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            var hearingFee = caseData.getHearingFee();
            hearingFee.setCalculatedAmountInPence(BigDecimal.ZERO);
            helpWithFeesDetails.setRemissionAmount(hearingFeeAmount);

            updatedData.hearingFee(hearingFee);
            updatedData.hwFeesDetails(helpWithFeesDetails);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }
}
