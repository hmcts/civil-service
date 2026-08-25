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
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

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
            callbackKey(CallbackType.ABOUT_TO_START), this::aboutToStart,
            callbackKey(CallbackType.MID, "enter-info"), this::checkEnterInfo,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::updateBusinessProcessToReady,
            callbackKey(CallbackType.SUBMITTED), this::buildSubmittedText
        );
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();
        if (caseData.getBreathing() == null || caseData.getBreathing().getEnter() == null) {
            return responseBuilder.errors(Collections.singletonList(
                "A claim must enter Breathing Space before it can be lifted."
            )).build();
        } else if (caseData.getBreathing().getLift() != null) {
            return responseBuilder.errors(Collections.singletonList(
                "This claim is not in Breathing Space."
            )).build();
        }

        prepopulateEndDate(caseData);

        objectMapper.findAndRegisterModules();
        return responseBuilder
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private static void prepopulateEndDate(CaseData caseData) {
        LocalDate startDate = caseData.getBreathing().getEnter().getStart();

        BreathingSpaceLiftInfo breathingSpaceLiftInfo = new BreathingSpaceLiftInfo();
        breathingSpaceLiftInfo.setExpectedEnd(LocalDate.now());
        caseData.getBreathing().setLift(breathingSpaceLiftInfo);

        if (caseData.getBreathing().getEnter().getType() == BreathingSpaceType.STANDARD) {
            breathingSpaceLiftInfo.setExpectedEnd(startDate.plusDays(60));
            caseData.getBreathing().setLift(breathingSpaceLiftInfo);
        }
    }

    private CallbackResponse checkEnterInfo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        int standardBSMaxDurationDays = 60;

        List<String> errors = new ArrayList<>();
        LocalDate startDate = caseData.getBreathing().getEnter().getStart();

        if (caseData.getBreathing().getEnter().getType() == BreathingSpaceType.STANDARD
            && caseData.getBreathing().getLift().getExpectedEnd().isAfter(startDate.plusDays(standardBSMaxDurationDays))) {
                errors.add("Standard breathing space cannot last for longer than 60 days");
        }

        if (startDate != null
            && (startDate.isAfter(caseData.getBreathing().getLift().getExpectedEnd()))
        || Objects.requireNonNull(startDate).isEqual(caseData.getBreathing().getLift().getExpectedEnd())) {
            errors.add("End date must be after " + DateFormatHelper
                .formatLocalDate(startDate, DateFormatHelper.DATE));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse updateBusinessProcessToReady(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        data.setBusinessProcess(BusinessProcess.ready(LIFT_BREATHING_SPACE_SPEC));
        data.getBreathing().setActive(NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data.toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildSubmittedText(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = "<br>We have sent you a confirmation email.";
        String header = format("# Breathing Space lifted%n## Claim number%n# %s", claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }
}
