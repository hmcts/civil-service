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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CASE;

@Service
@RequiredArgsConstructor
public class DismissCaseCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DISMISS_CASE);

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        if (featureToggleService.isCaseEventsEnabled()) {
            return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
                callbackKey(SUBMITTED), this::buildConfirmation
            );
        } else {
            return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
                callbackKey(SUBMITTED), this::emptyCallbackResponse
            );
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataUpdated = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(DISMISS_CASE))
            .hearingDate(null).hearingDueDate(null)
            .hearingNoticeList(null)
            .listingOrRelisting(null)
            .hearingLocation(null)
            .channel(null)
            .hearingTimeHourMinute(null)
            .hearingDuration(null)
            .information(null)
            .hearingNoticeListOther(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# The case has been dismissed\n## All parties have been notified")
            .confirmationBody("&nbsp;")
            .build();
    }

}
