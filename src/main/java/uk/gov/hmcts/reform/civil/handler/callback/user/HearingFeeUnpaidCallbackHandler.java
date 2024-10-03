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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;

@Service
@RequiredArgsConstructor
public class HearingFeeUnpaidCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(HEARING_FEE_UNPAID);

    private final ObjectMapper mapper;

    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::setCaseDismissedHearingFeeDueDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setCaseDismissedHearingFeeDueDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(HEARING_FEE_UNPAID))
            .caseDismissedHearingFeeDueDate(time.now())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(mapper))
            .state(CASE_DISMISSED.name())
            .build();
    }
}
