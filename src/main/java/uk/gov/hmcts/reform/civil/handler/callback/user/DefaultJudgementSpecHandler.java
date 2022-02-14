package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.db.DbIdGenerator;
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
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.service.FeesService;

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
    private final InterestCalculator interestCalculator;
    private static final int COMMENCEMENT_FIXED_COST_60 = 60;
    private static final int COMMENCEMENT_FIXED_COST_80 = 80;
    private static final int COMMENCEMENT_FIXED_COST_90 = 90;
    private static final int COMMENCEMENT_FIXED_COST_110 = 110;
    private static final int ENTRY_FIXED_COST_22 = 22;
    private static final int ENTRY_FIXED_COST_30 = 30;
    private final FeesService feesService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showCertifyStatementSpec"), this::checkStatus,
            callbackKey(MID, "claimPartialPayment"), this::partialPayment,
            callbackKey(MID, "repaymentBreakdown"), this::repaymentBreakdownCalculate,
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

    private BigDecimal calculateFixedCosts(CaseData caseData) {
        int fixedCost = 0;
        int totalClaimAmount = caseData.getTotalClaimAmount().intValue();
        if (totalClaimAmount > 25 && totalClaimAmount<=5000) {
            if(totalClaimAmount<= 500){
                fixedCost = COMMENCEMENT_FIXED_COST_60;
            }else if (totalClaimAmount <= 1000){
                fixedCost = COMMENCEMENT_FIXED_COST_80;
            }else{
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
        var claimFeePennies = claimfee.getCalculatedAmountInPence();
        var claimFeePounds = MonetaryConversions.penniesToPounds(claimFeePennies);
        BigDecimal fixedCost = calculateFixedCosts(caseData);
        BigDecimal partialPaymentPounds = new BigDecimal(0);

        //Check if partial payment was selected by user, and assign value if so.
        if (caseData.getPartialPayment() == YesOrNo.YES) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }

        //calculate the relevant total, total claim value + interest if any, claim fee for case,
        // and subtract any partial payment
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest)
            .add(claimFeePounds);
        if(caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES){
            subTotal = subTotal.add(fixedCost);
        }
        BigDecimal theOverallTotal = subTotal.subtract(partialPaymentPounds);

        //for assigning values to their relevant description
        String repaymentInterest;
        String repaymentPartial;
        String repaymentFixedCost;

        //creates  the text on the page, based on calculated values
        StringBuilder  repaymentBreakdown = new StringBuilder("The judgment will order the defendant to pay £").append(theOverallTotal).append(" including the claim fee and interest, if applicable, as shown.")
            .append("\n").append("### Claim Amount \n £").append(caseData.getTotalClaimAmount());

        if(interest.compareTo(BigDecimal.ZERO) != 0){
            repaymentInterest = "\n ### Claim interest \n" + "£" + interest;
            repaymentBreakdown.append(repaymentInterest);
        }

        if(caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES){
            repaymentFixedCost = "\n ### Fixed costs \n" + "£" + fixedCost;
            repaymentBreakdown = repaymentBreakdown.append(repaymentFixedCost);
        }

        repaymentBreakdown.append("\n").append("### Claim fee \n £").append(claimFeePounds).append("\n ## Subttotal \n £").append(subTotal)
            .append("\n");

        if (caseData.getPartialPayment() == YesOrNo.YES) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
            repaymentPartial = "\n ### Amount already paid \n" + "£" + partialPaymentPounds;
            repaymentBreakdown =  repaymentBreakdown.append(repaymentPartial);
        }

        repaymentBreakdown.append("\n ## Total still owed \n £").append(theOverallTotal);

        caseDataBuilder.repaymentSummaryObject(repaymentBreakdown.toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
        }
    }
