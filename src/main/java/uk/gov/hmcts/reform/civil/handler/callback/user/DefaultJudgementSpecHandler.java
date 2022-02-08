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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultJudgementSpecHandler extends CallbackHandler {

    public static final String NOT_VALID_DJ = "The Claim  is not eligible for Default Judgment util %s";
    public static final String CPR_REQUIRED_INFO = "<br />You can only request default judgment if:"
        + "%n%n * The time for responding to the claim has expired. "
        + "%n%n * The Defendant has not responded to the claim."
        + "%n%n * There is no outstanding application by the Defendant to strike out the claim for summary judgment."
        + "%n%n * The Defendant has not satisfied the whole claim, including costs."
        + "%n%n * The Defendant has not filed an admission together with request for time to pay."
        + "%n%n You can make another default judgment request when you know all these statements have been met.";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT_SPEC);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showCertifyStatementSpec"), this::checkStatus,
            callbackKey(MID, "claimPartialPayment"), this::partialPayment,
            callbackKey(MID, "claimPaymentDate"), this::validatePaymentDateDeadline,
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
            .confirmationHeader(getHeader())
            .confirmationBody(getBody())
            .build();
    }

    private String getHeader() {
        return format("# You cannot request default judgment");
    }

    private String getBody() {
        return format(CPR_REQUIRED_INFO);
    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (nonNull(caseData.getRespondent1ResponseDeadline())
            && caseData.getRespondent1ResponseDeadline().isAfter(LocalDateTime.now())) {
            String formattedDeadline = formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT);
            errors.add(format(NOT_VALID_DJ, formattedDeadline));
        }
        List<String> listData = new ArrayList<>();
        listData.add(caseData.getRespondent1().getIndividualFirstName() + " "
                         + caseData.getRespondent1().getIndividualLastName());
        caseDataBuilder.defendantDetailsSpec(DynamicList.fromList(listData));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.size() == 0
                      ? caseDataBuilder.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse checkStatus(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse partialPayment(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var totalIncludeInterest = caseData.getTotalClaimAmount().doubleValue() + caseData.getTotalInterest().doubleValue();
        List<String> errors = new ArrayList<>();

        if (caseData.getPartialPayment() == YesOrNo.YES) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            var partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies).doubleValue();
            if (partialPaymentPounds >= totalIncludeInterest) {
                errors.add("The amount already paid exceeds the full claim amount");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();

    }

    private CallbackResponse validatePaymentDateDeadline(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        /*Create Case field based on CIV-776 */
        if (checkPastDateValidation(caseData.getPaymentSetDate())) {
            errors.add("Payment Date cannot be past date");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

}
