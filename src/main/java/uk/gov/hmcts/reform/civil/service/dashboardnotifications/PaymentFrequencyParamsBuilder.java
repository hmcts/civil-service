package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;

@Component
public class PaymentFrequencyParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getJoJudgmentRecordReason() != null
            && caseData.getJoJudgmentRecordReason().equals(JudgmentRecordedReason.DETERMINATION_OF_MEANS)) {
            params.put("paymentFrequencyMessage", getPaymentFrequencyMessage(caseData, EN).toString());
            params.put("paymentFrequencyMessageCy", getPaymentFrequencyMessage(caseData, WELSH).toString());
        }
    }

    private StringBuilder getPaymentFrequencyMessage(CaseData caseData, String language) {
        PaymentPlanSelection paymentPlanType = caseData.getJoPaymentPlan().getType();
        StringBuilder paymentFrequencyMessage = new StringBuilder();
        BigDecimal totalAmount = new BigDecimal(caseData.getJoAmountOrdered());

        if ((caseData.getJoAmountCostOrdered() != null) && !caseData.getJoAmountCostOrdered().isEmpty()) {
            BigDecimal totalAmountCost = new BigDecimal(caseData.getJoAmountCostOrdered());
            totalAmount = totalAmount.add(totalAmountCost);
        }

        JudgmentInstalmentDetails instalmentDetails = caseData.getJoInstalmentDetails();

        if (PaymentPlanSelection.PAY_IN_INSTALMENTS.equals(paymentPlanType) && EN.equals(language)) {
            paymentFrequencyMessage.append("You must pay the claim amount of £")
                .append(MonetaryConversions.penniesToPounds(totalAmount).toString())
                .append(" ")
                .append(getStringPaymentMessage(instalmentDetails));
        } else {
            paymentFrequencyMessage.append("Rhaid i chi dalu swm yr hawliad, sef £")
                .append(MonetaryConversions.penniesToPounds(totalAmount).toString())
                .append(" ")
                .append(getStringPaymentMessageInWelsh(instalmentDetails));
        }
        return paymentFrequencyMessage;
    }

}
