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
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.dq.Hearing;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.unspec.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.unspec.validation.interfaces.WitnessesValidator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RespondToClaimCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE);

    private final DateOfBirthValidator dateOfBirthValidator;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;

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
            callbackKey(MID, "experts"), this::validateRespondentDqExperts,
            callbackKey(MID, "witnesses"), this::validateRespondentDqWitnesses,
            callbackKey(MID, "upload"), this::emptyCallbackResponse,
            callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth,
            callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Hearing hearing = caseData.getRespondent1DQ().getHearing();
        List<String> errors = unavailableDateValidator.validate(hearing);

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

    private CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();

        CaseData updatedData = caseData.toBuilder()
            .respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth)
            .build();

        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(null)
            .respondent1DQ(dq)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate, AllocatedTrack allocatedTrack) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate, allocatedTrack);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = format(
            "<br />The claimant has until %s to proceed. We will let you know when they respond.",
            formatLocalDateTime(responseDeadline, DATE)
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# You've submitted your response%n## Claim number: %s", claimNumber))
            .confirmationBody(body)
            .build();
    }
}
