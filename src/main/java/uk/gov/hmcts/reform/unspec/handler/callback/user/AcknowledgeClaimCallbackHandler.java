package uk.gov.hmcts.reform.unspec.handler.callback.user;

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
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.service.WorkingDayIndicator;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@Service
@RequiredArgsConstructor
public class AcknowledgeClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ACKNOWLEDGE_CLAIM);

    public static final String CONFIRMATION_SUMMARY = "<br />You need to respond before 4pm on %s."
        + "\n\n[Download the Acknowledgement of Claim form](%s)";

    private final DateOfBirthValidator dateOfBirthValidator;
    private final WorkingDayIndicator workingDayIndicator;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "confirm-details"), this::validateDateOfBirth,
            callbackKey(ABOUT_TO_SUBMIT), this::setNewResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        List<String> errors = dateOfBirthValidator.validate(respondent);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setNewResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getRespondentSolicitor1ResponseDeadline();
        LocalDate newResponseDate = workingDayIndicator.getNextWorkingDay(responseDeadline.plusDays(14).toLocalDate());
        CaseData caseDataUpdated = caseData.toBuilder()
            .respondentSolicitor1ResponseDeadline(newResponseDate.atTime(MID_NIGHT))
            .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String body = format(
            CONFIRMATION_SUMMARY,
            formatLocalDateTime(caseData.getRespondentSolicitor1ResponseDeadline(), DATE),
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference())
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You've acknowledged claim")
            .confirmationBody(body)
            .build();
    }
}
