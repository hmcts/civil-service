package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class EnterBreathingSpaceSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.ENTER_BREATHING_SPACE_SPEC);

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_START), this::checkCanEnter,
            callbackKey(CallbackType.MID, "enter-info"), this::checkEnterInfo,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(CallbackType.SUBMITTED), this::buildSubmittedText
        );
    }

    private CallbackResponse checkCanEnter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();
        if (caseData.getEnterBreathing() != null) {
            responseBuilder.errors(Collections.singletonList(
                "A claim can enter breathing space only once."
            ));
        }
        return responseBuilder.build();
    }

    private CallbackResponse checkEnterInfo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = new ArrayList<>();

        if (caseData.getEnterBreathing().getStart() != null
            && caseData.getEnterBreathing().getStart().isAfter(LocalDate.now())) {
            errors.add("Start date must be today or before.");
        }

        if (caseData.getEnterBreathing().getExpectedEnd() != null
            && !caseData.getEnterBreathing().getExpectedEnd().isAfter(LocalDate.now())) {
            errors.add("Expected end date must be in the future.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse buildSubmittedText(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = "<br>We have sent you a confirmation email."
            + "<h2 class=\"govuk-heading-m\">What happens next</h2>Breathing space will now be active until you "
            + "<u>lift breathing space.</u>";

        String header = format("# Breathing space entered%n## Claim number%n# %s", claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }
}
