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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.BreathingSpaceUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ENTER_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class EnterBreathingSpaceSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.ENTER_BREATHING_SPACE_SPEC);

    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_START), this::validateCanEnterBreathingSpace,
            callbackKey(CallbackType.MID, "enter-info"), this::validateBreathingSpaceEnterInfo,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::prepareEnterBreathingSpaceSubmit,
            callbackKey(CallbackType.SUBMITTED), this::buildConfirmationResponse
        );
    }

    private CallbackResponse validateCanEnterBreathingSpace(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(BreathingSpaceUtils.getCannotEnterBreathingSpaceReason(callbackParams.getCaseData())
                        .map(Collections::singletonList)
                        .orElse(null))
            .build();
    }

    private CallbackResponse validateBreathingSpaceEnterInfo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = new ArrayList<>();

        if (caseData.getBreathing().getEnter().getStart() != null
            && caseData.getBreathing().getEnter().getStart().isAfter(LocalDate.now())) {
            errors.add("Start date must be today or before.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmationResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = "<br>We have sent you a confirmation email."
            + "<h2 class=\"govuk-heading-m\">What happens next</h2><p>Breathing space will now be active until you "
            + "<u>lift Breathing Space.</u></p>";

        String header = format("# Breathing Space Entered%n## Claim number%n# %s", claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private CallbackResponse prepareEnterBreathingSpaceSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getBreathing() != null
            && caseData.getBreathing().getEnter() != null
            && caseData.getBreathing().getEnter().getStart() == null) {
            caseData.getBreathing().getEnter().setStart(LocalDate.now());
        }

        BreathingSpaceUtils.addEnteredBreathingSpaceToHistory(caseData);

        caseData.setBusinessProcess(BusinessProcess.ready(ENTER_BREATHING_SPACE_SPEC));
        caseData.getBreathing().setActive(YES);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
