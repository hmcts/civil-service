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
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.service.WorkingDayIndicator;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ACKNOWLEDGE_SERVICE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@Service
@RequiredArgsConstructor
public class AcknowledgeServiceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ACKNOWLEDGE_SERVICE);
    private static final String RESPONDENT = "respondent1";
    private static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";

    private final ObjectMapper mapper;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final WorkingDayIndicator workingDayIndicator;

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.MID, this::validateDateOfBirth,
            CallbackType.ABOUT_TO_SUBMIT, this::setNewResponseDeadline,
            CallbackType.SUBMITTED, this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        Party respondent = mapper.convertValue(data.get(RESPONDENT), Party.class);
        List<String> errors = dateOfBirthValidator.validate(respondent);

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .errors(errors)
                   .build();
    }

    private CallbackResponse setNewResponseDeadline(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        LocalDateTime responseDeadline = mapper.convertValue(data.get(RESPONSE_DEADLINE), LocalDateTime.class);

        LocalDate newResponseDate = workingDayIndicator.getNextWorkingDay(responseDeadline.plusDays(14).toLocalDate());

        data.put(RESPONSE_DEADLINE, newResponseDate.atTime(MID_NIGHT));

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        LocalDateTime responseDeadline = mapper.convertValue(data.get(RESPONSE_DEADLINE), LocalDateTime.class);

        String formattedDeemedDateOfService = formatLocalDateTime(responseDeadline, DATE);
        String acknowledgmentOfServiceForm = "http://www.google.com";

        String body = format("<br />You need to respond before 4pm on %s."
                                 + "\n\n[Download the Acknowledgement of Service form](%s)",
                             formattedDeemedDateOfService, acknowledgmentOfServiceForm
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You've acknowledged service")
            .confirmationBody(body)
            .build();
    }
}
