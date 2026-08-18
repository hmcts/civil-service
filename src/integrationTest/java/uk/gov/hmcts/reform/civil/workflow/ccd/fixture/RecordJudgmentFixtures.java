package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RecordJudgmentFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";

    private RecordJudgmentFixtures() {
    }

    public static CaseData recordJudgmentPayImmediately() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "100000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "10200");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
        });
    }

    public static CaseData recordJudgmentPayByDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "100000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "10200");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_BY_DATE)
                .setPaymentDeadlineDate(LocalDate.now().plusDays(60)));
        });
    }

    public static CaseData recordJudgmentPayByInstalments() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "100000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "10200");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IN_INSTALMENTS));
            CaseDataTemplates.set(template, "joInstalmentDetails", new JudgmentInstalmentDetails()
                .setAmount("25000")
                .setStartDate(LocalDate.now().plusDays(30))
                .setPaymentFrequency(PaymentFrequency.MONTHLY));
        });
    }

    public static CaseData recordJudgmentDeterminationOfMeans() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "100000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "10200");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.DETERMINATION_OF_MEANS);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
        });
    }

    public static CaseData recordJudgmentWithFutureOrderDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().plusDays(5));
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
        });
    }

    public static CaseData recordJudgmentWithExistingLiveJudgment() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
        });
    }
}
