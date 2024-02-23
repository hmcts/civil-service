package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@Service
@RequiredArgsConstructor
public class InvalidHwFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(INVALID_HWF_REFERENCE);
    private final ObjectMapper objectMapper;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
        callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedCaseData = setUpBusinessProcess(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData setUpBusinessProcess(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
                .businessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME));

        if (caseData.isHWFTypeHearing()) {
            HelpWithFeesDetails hearingFeeDetails =
                Optional.ofNullable(caseData.getHearingHwfDetails()).orElse(new HelpWithFeesDetails());
            updatedData.hearingHwfDetails(hearingFeeDetails.toBuilder().hwfCaseEvent(INVALID_HWF_REFERENCE).build());
        }
        if (caseData.isHWFTypeClaimIssued()) {
            HelpWithFeesDetails claimIssuedHwfDetails =
                Optional.ofNullable(caseData.getClaimIssuedHwfDetails()).orElse(new HelpWithFeesDetails());
            updatedData.claimIssuedHwfDetails(claimIssuedHwfDetails.toBuilder().hwfCaseEvent(INVALID_HWF_REFERENCE).build());
        }
        return updatedData.build();
    }
}
