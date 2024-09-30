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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_STAY;

@Service
@RequiredArgsConstructor
public class ManageStayCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MANAGE_STAY);

    private final FeatureToggleService featureToggleService;

    private static final String HEADER_CONFIRMATION_LIFT_STAY = "# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified";
    private static final String HEADER_CONFIRMATION_REQUEST_UPDATE = "# You have requested an update on \n\n # this case \n\n ## All parties have been notified";
    private static final String BODY_CONFIRMATION = "&nbsp;";

    private final ObjectMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit,
            callbackKey(SUBMITTED), this::handleSubmitted
        );
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams params) {
        return featureToggleService.isCaseEventsEnabled() ? manageStay(params) : emptyCallbackResponse(params);
    }

    private CallbackResponse handleSubmitted(CallbackParams params) {
        return featureToggleService.isCaseEventsEnabled() ? addConfirmationScreen(params) : emptyCallbackResponse(params);
    }

    private CallbackResponse manageStay(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(mapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse addConfirmationScreen(CallbackParams callbackParams) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(HEADER_CONFIRMATION_LIFT_STAY)
            .confirmationBody(BODY_CONFIRMATION)
            .build();

    }

}
