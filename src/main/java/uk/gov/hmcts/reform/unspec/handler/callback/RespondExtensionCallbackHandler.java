package uk.gov.hmcts.reform.unspec.handler.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RESPOND_EXTENSION;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@Service
@RequiredArgsConstructor
public class RespondExtensionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPOND_EXTENSION);
    public static final String COUNTER_DEADLINE = "respondentSolicitor1claimResponseExtensionCounterDate";
    public static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";
    public static final String PROPOSED_DEADLINE = "respondentSolicitor1claimResponseExtensionProposedDeadline";
    public static final String EXTENSION_REASON = "respondentSolicitor1claimResponseExtensionReason";
    public static final String PROVIDED_COUNTER_DATE = "respondentSolicitor1claimResponseExtensionCounter";
    public static final String PROPOSED_DEADLINE_ACCEPTED = "respondentSolicitor1claimResponseExtensionAccepted";
    public static final String LEGACY_CASE_REFERENCE = "legacyCaseReference";

    private final ObjectMapper mapper;
    private final RequestExtensionValidator validator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prepopulateRequestReasonIfAbsent,
            callbackKey(MID, "counter"), this::validateRequestedDeadline,
            callbackKey(ABOUT_TO_SUBMIT), this::updateResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepopulateRequestReasonIfAbsent(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        data.putIfAbsent(EXTENSION_REASON, "No reason given");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private CallbackResponse validateRequestedDeadline(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        YesOrNo providedCounterDate = mapToYesOrNo(data, PROVIDED_COUNTER_DATE);
        List<String> errors = new ArrayList<>();

        if (providedCounterDate == YesOrNo.YES) {
            LocalDate extensionCounterDate = mapToDate(data, COUNTER_DEADLINE);
            LocalDateTime responseDeadline = mapToDateTime(data, RESPONSE_DEADLINE);

            errors = validator.validateProposedDeadline(extensionCounterDate, responseDeadline);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse updateResponseDeadline(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        YesOrNo proposedDeadlineAccepted = mapToYesOrNo(data, PROPOSED_DEADLINE_ACCEPTED);
        YesOrNo providedCounterDate = mapToYesOrNo(data, PROVIDED_COUNTER_DATE);
        LocalDate newDeadline;

        if (proposedDeadlineAccepted == YesOrNo.YES) {
            newDeadline = mapToDate(data, PROPOSED_DEADLINE);
            data.put(RESPONSE_DEADLINE, newDeadline.atTime(MID_NIGHT));
        }

        if (providedCounterDate == YesOrNo.YES) {
            newDeadline = mapToDate(data, COUNTER_DEADLINE);
            data.put(RESPONSE_DEADLINE, newDeadline.atTime(MID_NIGHT));
        }

        data.put("businessProcess", BusinessProcess.builder().activityId("ExtensionResponseHandling").build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        LocalDateTime responseDeadline = mapToDateTime(data, RESPONSE_DEADLINE);

        String claimNumber = data.get(LEGACY_CASE_REFERENCE).toString();

        String body = format(
            "<br />The defendant must respond before 4pm on %s", formatLocalDateTime(responseDeadline, DATE));

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(
                "# You've responded to the request for more time%n## Claim number: %s",
                claimNumber
            ))
            .confirmationBody(body)
            .build();
    }

    private YesOrNo mapToYesOrNo(Map<String, Object> data, String fieldName) {
        return mapper.convertValue(data.get(fieldName), YesOrNo.class);
    }

    private LocalDate mapToDate(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field), LocalDate.class);
    }

    private LocalDateTime mapToDateTime(Map<String, Object> data, String field) {
        return mapper.convertValue(data.get(field), LocalDateTime.class);
    }
}
