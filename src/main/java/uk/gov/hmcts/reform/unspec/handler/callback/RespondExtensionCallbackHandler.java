package uk.gov.hmcts.reform.unspec.handler.callback;

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
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
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
    public static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";
    public static final String EXTENSION_REASON = "respondentSolicitor1claimResponseExtensionReason";
    public static final String LEGACY_CASE_REFERENCE = "legacyCaseReference";

    private final RequestExtensionValidator validator;
    private final BusinessProcessService businessProcessService;

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
        CaseData caseData = callbackParams.getCaseData();
        YesOrNo providedCounterDate = caseData.getRespondentSolicitor1claimResponseExtensionCounter();
        List<String> errors = new ArrayList<>();

        if (providedCounterDate == YesOrNo.YES) {
            LocalDate extensionCounterDate = caseData.getRespondentSolicitor1claimResponseExtensionCounterDate();
            LocalDateTime responseDeadline = caseData.getRespondentSolicitor1ResponseDeadline();

            errors = validator.validateProposedDeadline(extensionCounterDate, responseDeadline);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse updateResponseDeadline(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        CaseData caseData = callbackParams.getCaseData();
        YesOrNo proposedDeadlineAccepted = caseData.getRespondentSolicitor1claimResponseExtensionAccepted();
        YesOrNo providedCounterDate = caseData.getRespondentSolicitor1claimResponseExtensionCounter();
        LocalDate newDeadline;

        if (proposedDeadlineAccepted == YesOrNo.YES) {
            newDeadline = caseData.getRespondentSolicitor1claimResponseExtensionProposedDeadline();
            data.put(RESPONSE_DEADLINE, newDeadline.atTime(MID_NIGHT));
        }

        if (providedCounterDate == YesOrNo.YES) {
            newDeadline = caseData.getRespondentSolicitor1claimResponseExtensionCounterDate();
            data.put(RESPONSE_DEADLINE, newDeadline.atTime(MID_NIGHT));
        }

        List<String> errors = businessProcessService.updateBusinessProcess(data, RESPOND_EXTENSION);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getRespondentSolicitor1ResponseDeadline();
        String claimNumber = caseData.getLegacyCaseReference();

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
}
