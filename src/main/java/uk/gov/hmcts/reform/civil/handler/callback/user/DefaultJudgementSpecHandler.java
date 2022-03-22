package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
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
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultJudgementSpecHandler extends CallbackHandler {

    public static final String NOT_VALID_DJ = "The Claim  is not eligible for Default Judgment util %s";
//    public static final String CPR_REQUIRED_INFO = "<br />You can only request default judgment if:"
//        + "%n%n * The time for responding to the claim has expired. "
//        + "%n%n * The Defendant has not responded to the claim."
//        + "%n%n * There is no outstanding application by the Defendant to strike out the claim for summary judgment."
//        + "%n%n * The Defendant has not satisfied the whole claim, including costs."
//        + "%n%n * The Defendant has not filed an admission together with request for time to pay."
//        + "%n%n You can make another default judgment request when you know all these statements have been met.";
    public static final String JUDGMENT_GRANTED_HEADER = "# Default Judgment Granted ";
    public static final String JUDGMENT_GRANTED = "<br /><a href=\"%s\" target=\"_blank\">Download  default judgment</a> "
        + "%n The defendant will be served the Default Judgment.";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT_SPEC);
    private static final int COMMENCEMENT_FIXED_COST_60 = 60;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_90 = 90;
    private static final int COMMENCEMENT_FIXED_COST_110 = 110;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;
    private final ObjectMapper objectMapper;
    private final InterestCalculator interestCalculator;
    private final FeesService feesService;
    BigDecimal theOverallTotal;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showCertifyStatementSpec"), this::checkStatus,
            callbackKey(MID, "claimPartialPayment"), this::partialPayment,
            callbackKey(MID, "repaymentBreakdown"), this::repaymentBreakdownCalculate,
            callbackKey(MID, "repaymentTotal"), this::overallTotalAndDate,
            callbackKey(MID, "repaymentValidate"), this::repaymentValidate,
            callbackKey(MID, "claimPaymentDate"), this::validatePaymentDateDeadline,
            callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm,
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
            .confirmationBody(getBody(callbackParams.getCaseData()))
            .build();
    }

    private String getHeader() {

        return format(JUDGMENT_GRANTED_HEADER);
        //return format("# You cannot request default judgment");
    }

    private String getBody(CaseData caseData) {

        return format(JUDGMENT_GRANTED, format(
            "/cases/case-details/%s#Claim documents",
            caseData.getCcdCaseReference()
        ));
       // return format(CPR_REQUIRED_INFO);
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

        var totalIncludeInterest = caseData.getTotalClaimAmount().doubleValue()
            + caseData.getTotalInterest().doubleValue();
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

    private BigDecimal calculateFixedCosts(CaseData caseData) {

        int fixedCost = 0;
        int totalClaimAmount = caseData.getTotalClaimAmount().intValue();
        if (totalClaimAmount > 25 && totalClaimAmount <= 5000) {
            if (totalClaimAmount <= 500) {
                fixedCost = COMMENCEMENT_FIXED_COST_60;
            } else if (totalClaimAmount <= 1000) {
                fixedCost = COMMENCEMENT_FIXED_COST_80;
            } else {
                fixedCost = COMMENCEMENT_FIXED_COST_90;
            }
            fixedCost = fixedCost + ENTRY_FIXED_COST_22;
        } else if (totalClaimAmount > 5000) {
            fixedCost = COMMENCEMENT_FIXED_COST_110 + ENTRY_FIXED_COST_30;
        }
        return new BigDecimal(fixedCost);
    }

    private CallbackResponse repaymentBreakdownCalculate(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        var claimfee = feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        var claimFeePounds = MonetaryConversions.penniesToPounds(claimfee.getCalculatedAmountInPence());
        BigDecimal fixedCost = calculateFixedCosts(caseData);
        StringBuilder repaymentBreakdown = buildRepaymentBreakdown(
            caseData,
            interest,
            claimFeePounds,
            fixedCost
        );

        caseDataBuilder.repaymentSummaryObject(repaymentBreakdown.toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @NotNull
    private StringBuilder buildRepaymentBreakdown(CaseData caseData, BigDecimal interest, BigDecimal claimFeePounds,
                                                  BigDecimal fixedCost) {

        BigDecimal partialPaymentPounds = getPartialPayment(caseData);
        //calculate the relevant total, total claim value + interest if any, claim fee for case,
        // and subtract any partial payment
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest)
            .add(claimFeePounds);
        if (caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES) {
            subTotal = subTotal.add(fixedCost);
        }
        theOverallTotal = subTotal.subtract(partialPaymentPounds);
        //creates  the text on the page, based on calculated values
        StringBuilder repaymentBreakdown = new StringBuilder("The judgment will order the defendant to pay £").append(
                theOverallTotal).append(", including the claim fee and interest, if applicable, as shown:")
            .append("\n").append("### Claim amount \n £").append(caseData.getTotalClaimAmount().setScale(2));

        if (interest.compareTo(BigDecimal.ZERO) != 0) {
            repaymentBreakdown.append("\n ### Claim interest amount \n").append("£").append(interest.setScale(2));
        }

        if (caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES) {
            repaymentBreakdown.append("\n ### Fixed cost amount \n").append("£").append(fixedCost.setScale(2));
        }
        repaymentBreakdown.append("\n").append("### Claim fee amount \n £").append(claimFeePounds.setScale(2)).append(
                "\n ## Subtotal \n £").append(subTotal.setScale(2))
            .append("\n");

        if (caseData.getPartialPayment() == YesOrNo.YES) {
            repaymentBreakdown.append("\n ### Amount already paid \n").append("£").append(
                partialPaymentPounds.setScale(2));
        }

        repaymentBreakdown.append("\n ## Total still owed \n £").append(theOverallTotal.setScale(2));
        return repaymentBreakdown;
    }

    private BigDecimal getPartialPayment(CaseData caseData) {

        BigDecimal partialPaymentPounds = new BigDecimal(0);
        //Check if partial payment was selected by user, and assign value if so.
        if (caseData.getPartialPayment() == YesOrNo.YES) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }
        return partialPaymentPounds;
    }

    private CallbackResponse overallTotalAndDate(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        //Set the hint date for repayment to be 30 days in the future
        String formattedDeadline = formatLocalDateTime(LocalDateTime.now().plusDays(30), DATE);
        caseDataBuilder.currentDatebox(formattedDeadline);
        //set the calculated repayment owed
        caseDataBuilder.repaymentDue(theOverallTotal.toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse repaymentValidate(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        //Check repayment amount requested is less than the overall claim amount
        var repayment = new BigDecimal(caseData.getRepaymentDue());
        var regularRepaymentAmountPennies = new BigDecimal(caseData.getRepaymentSuggestion());
        var regularRepaymentAmountPounds = MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies);

        if (regularRepaymentAmountPounds.compareTo(repayment) > 0) {
            errors.add("Regular payment cannot exceed the full claim amount");
        }

        LocalDate eligibleDate;
        formatLocalDate(eligibleDate = LocalDate.now().plusDays(30), DATE);
        if (caseData.getRepaymentDate().isBefore(eligibleDate.plusDays(1))) {
            errors.add("Selected date must be after " + formatLocalDate(eligibleDate, DATE));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(DEFAULT_JUDGEMENT_SPEC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}



