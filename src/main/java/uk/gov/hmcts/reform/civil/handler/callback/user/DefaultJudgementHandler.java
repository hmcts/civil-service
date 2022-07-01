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
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefaultJudgementHandler extends CallbackHandler {

    public static final String NOT_VALID_DJ = "The Claim  is not eligible for Default Judgment until %s";
    public static final String JUDGMENT_GRANTED = "<br /><a href=\"%s\" target=\"_blank\">Download  interim judgment</a> "
        + "%n%n Judgment has been entered and your case will be referred to a judge for directions.";
    public static final String JUDGMENT_REFERRED = "Your request will be referred to a judge and we will contact you "
        + "and tell you what happens next.";
    public static final String DISPOSAL_TEXT = "will be disposal hearing provided text";
    public static final String TRIAL_TEXT = "will be trial hearing provided text";
    public static final String JUDGMENT_REQUESTED = "# Judgment for damages to be decided requested ";
    public static final String JUDGMENT_GRANTED_HEADER = "# Judgment for damages to be decided Granted ";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showcertifystatement"), this::checkStatus,
            callbackKey(MID, "acceptCPR"), this::acceptCPR,
            callbackKey(MID, "HearingSupportRequirementsDJ"), this::validateDateValues,
            callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return JUDGMENT_REFERRED;
        } else {
            return format(JUDGMENT_GRANTED, format(
                "/cases/case-details/%s#Claim documents",
                caseData.getCcdCaseReference()
            ));
        }
    }

    private String getHeader(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return format(JUDGMENT_REQUESTED, caseData.getLegacyCaseReference());

        } else {
            return format(JUDGMENT_GRANTED_HEADER, caseData.getLegacyCaseReference());
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
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
            if (checkPastDateValidation(hearingDates.getHearingUnavailableFrom())
                || checkPastDateValidation(
                hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable Date cannot be past date");
            } else if (checkThreeMonthsValidation(hearingDates.getHearingUnavailableFrom())
                || checkThreeMonthsValidation(
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

    private CallbackResponse acceptCPR(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        var acceptance = callbackParams.getRequest().getCaseDetails().getData().get("CPRAcceptance");
        var acceptance2Def = callbackParams.getRequest().getCaseDetails().getData().get("CPRAcceptance2Def");
        if (Objects.isNull(acceptance) && Objects.isNull(acceptance2Def)) {
            errors.add("To apply for default judgment, all of the statements must apply to the defendant "
                           + "- if they do not apply, close this page and apply for default judgment when they do");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
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
        listData.add(getPartyNameBasedOnType(caseData.getRespondent1()));
        if (nonNull(caseData.getRespondent2())) {
            listData.add(getPartyNameBasedOnType(caseData.getRespondent2()));
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

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(DEFAULT_JUDGEMENT));

        var state = "PROCEEDS_IN_HERITAGE_SYSTEM";
        if (caseData.getRespondent2() == null || caseData.getRespondent2() != null
            && caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            state = "JUDICIAL_REFERRAL";
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(state)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
