package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RecordJudgmentFixtures {

    private RecordJudgmentFixtures() {
    }

    public static CaseData recordJudgmentPayImmediately() {
        return baseJoCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .build();
    }

    public static CaseData recordJudgmentPayByDate() {
        return baseJoCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joPaymentPlan(new JudgmentPaymentPlan()
                               .setType(PaymentPlanSelection.PAY_BY_DATE)
                               .setPaymentDeadlineDate(LocalDate.now().plusDays(60)))
            .build();
    }

    public static CaseData recordJudgmentPayByInstalments() {
        return baseJoCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IN_INSTALMENTS))
            .joInstalmentDetails(new JudgmentInstalmentDetails()
                                     .setAmount("25000")
                                     .setStartDate(LocalDate.now().plusDays(30))
                                     .setPaymentFrequency(PaymentFrequency.MONTHLY))
            .build();
    }

    public static CaseData recordJudgmentDeterminationOfMeans() {
        return baseJoCase()
            .joJudgmentRecordReason(JudgmentRecordedReason.DETERMINATION_OF_MEANS)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .build();
    }

    public static CaseData recordJudgmentWithFutureOrderDate() {
        return baseJoCase()
            .joOrderMadeDate(LocalDate.now().plusDays(5))
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .build();
    }

    public static CaseData recordJudgmentWithExistingLiveJudgment() {
        return baseJoCase()
            .joIsLiveJudgmentExists(YesOrNo.YES)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .build();
    }

    private static CaseData.CaseDataBuilder<?, ?> baseJoCase() {
        return CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .joOrderMadeDate(LocalDate.now().minusDays(5))
            .joAmountOrdered("100000")
            .joAmountCostOrdered("10200")
            .joIsRegisteredWithRTL(YesOrNo.YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"));
    }
}
