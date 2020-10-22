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
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RespondToClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE);
    public static final String CLAIMANT_RESPONSE_DEADLINE = "applicantSolicitorResponseDeadlineToRespondentSolicitor1";

    private final DateOfBirthValidator dateOfBirthValidator;
    private final BusinessProcessService businessProcessService;
    private final UnavailableDateValidator unavailableDateValidator;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "confirm-details"), this::validateDateOfBirth,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(MID, "upload"), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::setClaimantResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<Element<UnavailableDate>> unavailableDates =
            ofNullable(caseData.getRespondent1DQ().getHearing().getUnavailableDates()).orElse(emptyList());
        List<String> errors = unavailableDateValidator.validate(unavailableDates);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        List<String> errors = dateOfBirthValidator.validate(respondent);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setClaimantResponseDeadline(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        //TODO: There will be in separate ticket for response deadline when requirement is confirmed
        LocalDate claimantResponseDeadLine = LocalDate.now();
        data.put(CLAIMANT_RESPONSE_DEADLINE, claimantResponseDeadLine.atTime(16, 0));
        List<String> errors = businessProcessService.updateBusinessProcess(data, DEFENDANT_RESPONSE);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getApplicantSolicitorResponseDeadlineToRespondentSolicitor1();

        String claimNumber = "TBC";

        String body = format(
            "<br />The claimant has until %s to proceed. We will let you know when they respond.",
            formatLocalDateTime(responseDeadline, DATE)
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(
                "# You've submitted your response%n## Claim number: %s",
                claimNumber
            ))
            .confirmationBody(body)
            .build();
    }
}
