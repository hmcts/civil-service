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
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;

@Service
@RequiredArgsConstructor
public class FullRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(FULL_REMISSION_HWF);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
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
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();

        if (caseData.isHWFTypeClaimIssued() && claimFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                .ifPresentOrElse(
                    claimIssuedHwfDetails -> updatedData.claimIssuedHwfDetails(
                        claimIssuedHwfDetails.builder().remissionAmount(claimFeeAmount).build()
                    ),
                    () -> updatedData.claimIssuedHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(claimFeeAmount).build()
                    )
                );
        } else if (caseData.isHWFTypeHearing() && hearingFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            Optional.ofNullable(caseData.getHearingHwfDetails())
                .ifPresentOrElse(
                    hearingHwfDetails -> updatedData.hearingHwfDetails(
                        hearingHwfDetails.builder().remissionAmount(hearingFeeAmount).build()
                    ),
                    () -> updatedData.hearingHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(hearingFeeAmount).build()
                    )
                );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
