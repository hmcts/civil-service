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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultJudgementHandler extends CallbackHandler {
    public static final String NOT_VALID_DJ = "The Claim  is not eligible for Default Judgment util %s";
    public static final String CPR_REQUIRED_INFO = "<br />You can only request default judgment if:"
        + "%n%n * The time for responding to the claim has expired. "
        + "%n%n * The Defendant has not responded to the claim."
        + "%n%n * There is no outstanding application by the Defendant to strike out the claim for summary judgment."
        + "%n%n * The Defendant has not satisfied the whole claim, including costs."
        + "%n%n * The Defendant has not filed an admission together with request for time to pay."
        + "%n%n You can make another default judgment request when you know all these statements have been met.";
    public static final String HEADER = "# You cannot request default judgment";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showcertifystatement"), this::checkStatus,
            callbackKey(MID, "HearingSupportRequirementsDJ"), this::validateDateValues,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(HEADER))
            .confirmationBody(format(CPR_REQUIRED_INFO))
            .build();
    }




    private CallbackResponse validateDateValues(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        List<Element<HearingDates>> hearingDatesElement = caseData.getHearingSupportRequirementsDJ().getHearingDates();
        List<String> errors = (Objects.isNull(hearingDatesElement)) ? null :
            isValidRange(hearingDatesElement);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();

    }

    private List<String> isValidRange(List<Element<HearingDates>> hearingDatesElement) {
        List<String> errors = new ArrayList<>();
        hearingDatesElement.forEach(element -> {
            HearingDates hearingDates = element.getValue();
            if (checkPastDateValidation(hearingDates.getHearingUnavailableFrom()) || checkPastDateValidation(
                hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable Date cannot be past date");
            } else if (checkThreeMonthsValidation(hearingDates.getHearingUnavailableFrom()) || checkThreeMonthsValidation(
                hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable Dates must be within the next 3 months.");
            } else if (hearingDates.getHearingUnavailableFrom()
                .isAfter(hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable From Date should be less than To Date");
            }

        });
        return errors;
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

    private boolean checkThreeMonthsValidation(LocalDate localDate) {
        return localDate != null && localDate.isAfter(LocalDate.now().plusMonths(3));
    }


    private CallbackResponse checkStatus(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.bothDefendants("One");
        if (caseData.getDefendantDetails().getValue().getLabel().startsWith("Both")) {
            caseDataBuilder.bothDefendants(caseData.getDefendantDetails().getValue().getLabel());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (nonNull(caseData.getRespondent1ResponseDeadline()) && caseData.getRespondent1ResponseDeadline().isAfter(
            LocalDateTime.now())) {
            String formattedDeadline = formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT);
            errors.add(format(NOT_VALID_DJ, formattedDeadline));
        }
        List<String> listData = new ArrayList<>();
        listData.add(caseData.getRespondent1().getIndividualFirstName() + " " + caseData.getRespondent1().getIndividualLastName());
        if (nonNull(caseData.getRespondent2())) {
            listData.add(caseData.getRespondent2().getIndividualFirstName() + " " + caseData.getRespondent2().getIndividualLastName());
            listData.add("Both Defendants");
            caseDataBuilder.defendantDetails(DynamicList.fromList(listData));
        }
        caseDataBuilder.defendantDetails(DynamicList.fromList(listData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.size() == 0
                      ? caseDataBuilder.build().toMap(objectMapper) : null)
            .build();
    }


}
