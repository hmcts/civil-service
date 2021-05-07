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
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.unspec.service.Time;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class NotifyClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_DEFENDANT_OF_CLAIM);
    public static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation has "
        + "been notified and granted access to this claim.%n%n"
        + "You must notify the defendant with the claim details by %s";

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
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
        LocalDateTime claimNotificationDate = time.now();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM))
            .claimNotificationDate(claimNotificationDate);

        LocalDateTime deadline = getDeadline(claimNotificationDate);
        LocalDateTime claimNotificationDeadline = caseData.getClaimNotificationDeadline();

        if (deadline.isAfter(claimNotificationDeadline)) {
            caseDataBuilder.claimDetailsNotificationDeadline(claimNotificationDeadline);
        } else {
            caseDataBuilder.claimDetailsNotificationDeadline(deadline);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private LocalDateTime getDeadline(LocalDateTime claimNotificationDate) {
        return deadlinesCalculator.plus14DaysAt4pmDeadline(claimNotificationDate.toLocalDate());
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String formattedDeadline = formatLocalDateTime(caseData.getClaimDetailsNotificationDeadline(), DATE_TIME_AT);

        String body = format(CONFIRMATION_SUMMARY, formattedDeadline) + exitSurveyContentService.applicantSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# Notification of claim sent%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }
}
