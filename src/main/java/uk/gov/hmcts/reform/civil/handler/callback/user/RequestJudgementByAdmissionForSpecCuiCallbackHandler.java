package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_JUDGEMENT_ADMISSION_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestJudgementByAdmissionForSpecCuiCallbackHandler extends CallbackHandler {

    private static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);
    private static final String NOT_VALID_DJ_BY_ADMISSION = "The Claim is not eligible for Request Judgment By Admission until %s.";
    private static final String JUDGEMENT_BY_COURT = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.";
    private static final String JUDGEMENT_ORDER = "The judgment will order the defendant to pay £%s , including the claim fee and interest, if applicable, as shown:";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_JUDGEMENT_ADMISSION_SPEC);
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility)
            .put(callbackKey(MID, "set-up-ccj-amount-summary"), this::buildJudgmentAmountSummaryDetails)
            .put(callbackKey(MID, "validate-amount-paid"), this::validateAmountPaid)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::emptySubmittedCallbackResponse)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        ArrayList<String> errors = new ArrayList<>();
        if (nonNull(caseData.getRespondent1ResponseDate())
            && caseData.getRespondent1ResponseDate()
            .toLocalDate().plusDays(5).atTime(END_OF_BUSINESS_DAY).isAfter(LocalDateTime.now())) {
            String formattedDeadline = formatLocalDateTime(
                caseData.getRespondent1ResponseDate().toLocalDate().plusDays(5).atTime(END_OF_BUSINESS_DAY), DATE_TIME_AT);
            errors.add(format(NOT_VALID_DJ_BY_ADMISSION, formattedDeadline));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.size() == 0
                      ? caseDataBuilder.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse buildJudgmentAmountSummaryDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        BigDecimal claimAmount = caseData.getTotalClaimAmount();
        if (YesOrNo.YES.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())) {
            claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        }
        BigDecimal claimFee =  MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());
        BigDecimal paidAmount = (caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption() == YesOrNo.YES)
            ? MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()) : ZERO;
        BigDecimal fixedCost = caseData.getUpFixedCostAmount(claimAmount, caseData);
        BigDecimal subTotal =  claimAmount.add(claimFee).add(caseData.getTotalInterest()).add(fixedCost);
        BigDecimal finalTotal = subTotal.subtract(paidAmount);
        String ccjJudgmentStatement;
        if (YesOrNo.NO.equals(caseData.getSpecRespondent1Represented())
            && featureToggleService.isPinInPostEnabled()
            && MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            ccjJudgmentStatement = JUDGEMENT_BY_COURT;
        } else {
            ccjJudgmentStatement = String.format(JUDGEMENT_ORDER, subTotal);
        }

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(claimAmount)
            .ccjJudgmentAmountClaimFee(claimFee)
            .ccjJudgmentSummarySubtotalAmount(subTotal)
            .ccjJudgmentTotalStillOwed(finalTotal)
            .ccjJudgmentAmountInterestToDate(caseData.getTotalInterest())
            .ccjPaymentPaidSomeAmountInPounds(paidAmount)
            .ccjJudgmentFixedCostAmount(fixedCost)
            .ccjJudgmentStatement(ccjJudgmentStatement)
            .build();

        updatedCaseData.ccjPaymentDetails(ccjPaymentDetails);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateAmountPaid(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (caseData.isPaidSomeAmountMoreThanClaimAmount(caseData)) {
            errors.add("The amount paid must be less than the full claim amount.");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

}
