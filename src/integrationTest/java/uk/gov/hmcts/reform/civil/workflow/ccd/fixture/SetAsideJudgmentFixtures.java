package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

public final class SetAsideJudgmentFixtures {

    private SetAsideJudgmentFixtures() {
    }

    public static CaseData setAsideJudgmentError() {
        return baseSetAsideCase()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .joSetAsideOrderDate(LocalDate.now().minusDays(5))
            .build();
    }

    public static CaseData setAsideJudgeOrderAfterApplication() {
        return baseSetAsideCase()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .joSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION)
            .joSetAsideOrderDate(LocalDate.now().minusDays(5))
            .joSetAsideApplicationDate(LocalDate.now().minusDays(10))
            .build();
    }

    public static CaseData setAsideJudgeOrderAfterDefence() {
        return baseSetAsideCase()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .joSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE)
            .joSetAsideOrderDate(LocalDate.now().minusDays(5))
            .joSetAsideDefenceReceivedDate(LocalDate.now().minusDays(7))
            .build();
    }

    public static CaseData setAsideWithFutureOrderDate() {
        return baseSetAsideCase()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .joSetAsideOrderDate(LocalDate.now().plusDays(5))
            .build();
    }

    public static CaseData setAsideApplicationDateAfterOrderDate() {
        return baseSetAsideCase()
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .joSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION)
            .joSetAsideOrderDate(LocalDate.now().minusDays(10))
            .joSetAsideApplicationDate(LocalDate.now().minusDays(3))
            .build();
    }

    private static CaseData.CaseDataBuilder<?, ?> baseSetAsideCase() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joIsLiveJudgmentExists(YesOrNo.YES)
            .activeJudgment(activeJudgment());
    }

    private static JudgmentDetails activeJudgment() {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setIssueDate(LocalDate.now().minusDays(30))
            .setIsRegisterWithRTL(YesOrNo.YES)
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }
}
