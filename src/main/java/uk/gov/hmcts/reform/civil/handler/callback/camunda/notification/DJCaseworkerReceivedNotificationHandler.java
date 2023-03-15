package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.defaultjudgments.DefaultJudgmentSpecEmailConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CASEWORKER_DJ_RECEIVED;
import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCosts;

@Slf4j
@Service
@RequiredArgsConstructor
public class DJCaseworkerReceivedNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final InterestCalculator interestCalculator;
    private final FeesService feesService;
    private final DefaultJudgmentSpecEmailConfiguration defaultJudgmentSpecEmailConfiguration;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CASEWORKER_DJ_RECEIVED);
    private static final String REFERENCE_TEMPLATE_CASEWORKER = "default-judgment-caseworker-received-notification-%s";
    public static final String TASK_ID = "NotifyCaseworkerDJReceived";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDJApprovedCaseworker
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyDJApprovedCaseworker(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent2() != null
            && !caseData.getDefendantDetailsSpec().getValue()
            .getLabel().startsWith("Both")) {
            log.info("Default Judgment Spec email sent to: " + defaultJudgmentSpecEmailConfiguration.getReceiver());
            notificationService.sendMail(
                defaultJudgmentSpecEmailConfiguration.getReceiver(),
                notificationsProperties.getCaseworkerDefaultJudgmentRequested(),
                addProperties(caseData),
                String.format(
                    REFERENCE_TEMPLATE_CASEWORKER,
                    caseData.getLegacyCaseReference()
                )
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        final BigDecimal amountClaimed = getAmountClaimed(caseData);
        final BigDecimal amountOfCosts =  getAmountOfCosts(caseData);
        final BigDecimal partialPayment = getPartialPayment(caseData);
        final BigDecimal total = amountClaimed.add(amountOfCosts).subtract(partialPayment);
        return new HashMap<>(Map.of(
            CLAIM_NUMBER, caseData.getLegacyCaseReference(),
            PAYMENT_TYPE, getPaymentTypeField(caseData, total),
            AMOUNT_CLAIMED, amountClaimed.toString(),
            RESPONDENT, caseData.getDefendantDetailsSpec().getValue().getLabel(),
            AMOUNT_OF_COSTS, amountOfCosts.toString(),
            AMOUNT_PAID, partialPayment.toString(),
            AMOUNT_OF_JUDGMENT, total.toString()
        ));
    }

    private String getPaymentTypeField(CaseData caseData, BigDecimal total) {
        if (isNull(caseData.getPaymentTypeSelection())) {
            return "No payment type selected";
        }
        switch (caseData.getPaymentTypeSelection()) {
            case IMMEDIATELY:
                return "Immediately £" + total;
            case SET_DATE:
                return "In full by " + caseData.getPaymentSetDate();
            case REPAYMENT_PLAN:
                return "By installments of £" + getRepaymentAmount(caseData)
                    + " per " + getRepaymentFrequency(caseData);
            default:
                return "No payment type selected";
        }
    }

    private BigDecimal getAmountClaimed(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        return caseData.getTotalClaimAmount().add(interest);
    }

    private BigDecimal getAmountOfCosts(CaseData caseData) {
        var claimFee = feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        var claimFeePounds = MonetaryConversions.penniesToPounds(claimFee.getCalculatedAmountInPence());
        BigDecimal fixedCost = calculateFixedCosts(caseData);
        var subTotal = claimFeePounds;
        if (caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES) {
            subTotal = subTotal.add(fixedCost);
        }
        return subTotal;
    }

    private BigDecimal getPartialPayment(CaseData caseData) {
        if (caseData.getPartialPayment() == YesOrNo.YES) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            return MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getRepaymentAmount(CaseData caseData) {
        var repaymentAmountInPennies = new BigDecimal(caseData.getRepaymentSuggestion());
        return MonetaryConversions.penniesToPounds(repaymentAmountInPennies);
    }

    private String getRepaymentFrequency(CaseData caseData) {
        switch (caseData.getRepaymentFrequency()) {
            case ONCE_ONE_WEEK:
                return "week";
            case ONCE_TWO_WEEKS:
                return "two weeks";
            case ONCE_ONE_MONTH:
                return "month";
            default:
                return "";
        }
    }

}
