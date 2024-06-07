package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.SettleClaimHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

@Service
@RequiredArgsConstructor
public class SettleClaimCallbackHandler extends CallbackHandler {

    protected final ObjectMapper objectMapper;

    private static final List<CaseEvent> EVENTS = List.of(SETTLE_CLAIM);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::checkState,
            callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentPaidInFullDetails,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse checkState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        SettleClaimHelper.checkState(caseData, errors);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse saveJudgmentPaidInFullDetails(CallbackParams callbackParams) {
        CaseData caseDataBuilder = callbackParams.getCaseData().toBuilder().build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.toMap(objectMapper))
            .state(CASE_SETTLED.name())
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Claim marked as settled")
            .confirmationBody("<br />")
            .build();
    }
}
