package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STAY_CASE;

@Service
@RequiredArgsConstructor
public class StayCaseCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(STAY_CASE);

    private final FeatureToggleService featureToggleService;

    private static final String HEADER_CONFIRMATION = "# Stay added to the case \n\n ## All parties have been notified and any upcoming hearings must be cancelled";
    private static final String BODY_CONFIRMATION = "&nbsp;";

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isCaseEventsEnabled()
            ? Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::addConfirmationScreen
            )
            : Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::emptyCallbackResponse
            );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse addConfirmationScreen(CallbackParams callbackParams) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(HEADER_CONFIRMATION)
            .confirmationBody(BODY_CONFIRMATION)
            .build();

    }

}
