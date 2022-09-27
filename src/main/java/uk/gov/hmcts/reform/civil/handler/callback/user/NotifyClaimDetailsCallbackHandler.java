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
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class NotifyClaimDetailsCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS);
    private static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation has"
        + " been notified of the claim details.%n%n"
        + "They must respond by %s. Your account will be updated and you will be sent an email.";

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaimBackwardsCompatible,
            callbackKey(V_1, MID, "particulars-of-claim"), this::validateParticularsOfClaim,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime notificationDateTime = time.now();

        LocalDate notificationDate = notificationDateTime.toLocalDate();
        CaseData updatedCaseData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS))
            .claimDetailsNotificationDate(notificationDateTime)
            .respondent1ResponseDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime))
            .claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, notificationDate))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String formattedDeadline = formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT);

        String body = format(CONFIRMATION_SUMMARY, formattedDeadline) + exitSurveyContentService.applicantSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# Defendant notified%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }
}
