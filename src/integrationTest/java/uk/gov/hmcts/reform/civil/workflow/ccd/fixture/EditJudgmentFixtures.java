package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class EditJudgmentFixtures {

    private EditJudgmentFixtures() {
    }

    public static CaseData editDefaultJudgment() {
        return baseEditCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .activeJudgment(defaultJudgmentActiveJudgment())
            .build();
    }

    public static CaseData editJudgmentDeterminationOfMeans() {
        return baseEditCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.DETERMINATION_OF_MEANS)
            .activeJudgment(recordedJudgmentActiveJudgment())
            .build();
    }

    public static CaseData editJudgmentNoActiveJudgment() {
        return baseEditCase()
            .joIsLiveJudgmentExists(YesOrNo.NO)
            .activeJudgment(null)
            .build();
    }

    private static CaseData.CaseDataBuilder<?, ?> baseEditCase() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .joIsLiveJudgmentExists(YesOrNo.YES)
            .joOrderMadeDate(LocalDate.now().minusDays(5))
            .joAmountOrdered("120000")
            .joAmountCostOrdered("15000")
            .joIsRegisteredWithRTL(YesOrNo.YES)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }

    private static JudgmentDetails defaultJudgmentActiveJudgment() {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setIssueDate(LocalDate.now().minusDays(10))
            .setIsRegisterWithRTL(YesOrNo.YES)
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }

    private static JudgmentDetails recordedJudgmentActiveJudgment() {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.JUDGMENT_FOLLOWING_HEARING)
            .setIssueDate(LocalDate.now().minusDays(10))
            .setIsRegisterWithRTL(YesOrNo.NO)
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }
}
