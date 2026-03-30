package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;

@Component
public class CcjParamsBuilder extends DashboardNotificationsParamsBuilder {

    private static final String CCJ_PAYMENT_MESSAGE_EN = "ccjPaymentMessageEn";
    private static final String CCJ_PAYMENT_MESSAGE_CY = "ccjPaymentMessageCy";

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getActiveJudgment())
            && caseData.getActiveJudgment().getState().equals(ISSUED)
            && nonNull(caseData.getActiveJudgment().getPaymentPlan())) {
            updateCCJParams(caseData, params);
        }
    }

    private  void updateCCJParams(CaseData caseData, HashMap<String, Object> params) {
        JudgmentDetails judgmentDetails = caseData.getActiveJudgment();
        String totalAmount = judgmentDetails.getTotalAmount();
        params.put(
            "ccjDefendantAdmittedAmount",
            MonetaryConversions.penniesToPounds(new BigDecimal(totalAmount))
        );

        if (caseData.getActiveJudgment().getPaymentPlan().getType().equals(PAY_IN_INSTALMENTS)) {
            JudgmentInstalmentDetails instalmentDetails = judgmentDetails.getInstalmentDetails();
            params.put(CCJ_PAYMENT_MESSAGE_EN, getStringPaymentMessage(instalmentDetails));
            params.put(CCJ_PAYMENT_MESSAGE_CY, getStringPaymentMessageInWelsh(instalmentDetails));
        } else if (caseData.getActiveJudgment().getPaymentPlan().getType().equals(PAY_IMMEDIATELY)) {
            params.put(CCJ_PAYMENT_MESSAGE_EN, "immediately");
            params.put(CCJ_PAYMENT_MESSAGE_CY, "ar unwaith");
        } else {
            params.put(
                CCJ_PAYMENT_MESSAGE_EN,
                "by " + DateUtils.formatDate(judgmentDetails.getPaymentPlan().getPaymentDeadlineDate())
            );
            params.put(
                CCJ_PAYMENT_MESSAGE_CY,
                "erbyn " + DateUtils.formatDateInWelsh(judgmentDetails.getPaymentPlan().getPaymentDeadlineDate(), false)
            );
        }
    }
}
