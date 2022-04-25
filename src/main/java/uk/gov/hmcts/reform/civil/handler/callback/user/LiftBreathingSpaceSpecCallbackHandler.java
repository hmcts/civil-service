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
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;

@Service
@RequiredArgsConstructor
public class LiftBreathingSpaceSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(LIFT_BREATHING_SPACE_SPEC);

    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_START), this::checkCanEnter,
            callbackKey(CallbackType.MID, "enter-info"), this::checkEnterInfo,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::updateBusinessProcessToReady,
            callbackKey(CallbackType.SUBMITTED), this::buildSubmittedText
        );
    }

    private CallbackResponse checkCanEnter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();
        if (caseData.getBreathing() == null || caseData.getBreathing().getEnter() == null) {
            responseBuilder.errors(Collections.singletonList(
                "A claim must enter Breathing Space before it can be lifted."
            ));
        } else if (caseData.getBreathing().getLift() != null) {
            responseBuilder.errors(Collections.singletonList(
                "This claim is not in Breathing Space anymore."
            ));
        }

        return responseBuilder.build();
    }

    private CallbackResponse checkEnterInfo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = new ArrayList<>();

        if (caseData.getBreathing().getLift().getExpectedEnd() != null) {
            if (caseData.getBreathing().getLift().getExpectedEnd().isAfter(LocalDate.now())) {
                errors.add("End date must be today or in the past.");
            } else if (caseData.getBreathing().getEnter().getStart() != null
                && caseData.getBreathing().getEnter().getStart().isAfter(
                caseData.getBreathing().getLift().getExpectedEnd()
            )) {
                errors.add("End date must be after " + DateFormatHelper
                    .formatLocalDate(caseData.getBreathing().getEnter().getStart(), DateFormatHelper.DATE));
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse updateBusinessProcessToReady(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        CaseData.CaseDataBuilder caseDataBuilder = data.toBuilder()
            .businessProcess(BusinessProcess.ready(LIFT_BREATHING_SPACE_SPEC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildSubmittedText(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = "<br>We have sent you a confirmation email.";
        String header = format("# Breathing Space Lifted%n## Claim number%n# %s", claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }
}
