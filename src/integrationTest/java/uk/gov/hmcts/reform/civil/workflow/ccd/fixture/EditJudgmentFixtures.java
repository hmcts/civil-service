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
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class EditJudgmentFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";

    private EditJudgmentFixtures() {
    }

    public static CaseData editDefaultJudgment() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "120000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "15000");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
            CaseDataTemplates.set(template, "activeJudgment", defaultJudgmentActiveJudgment());
        });
    }

    public static CaseData editJudgmentDeterminationOfMeans() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joAmountOrdered", "120000");
            CaseDataTemplates.set(template, "joAmountCostOrdered", "15000");
            CaseDataTemplates.set(template, "joIsRegisteredWithRTL", YesOrNo.YES);
            CaseDataTemplates.set(template, "joJudgmentRecordReason", JudgmentRecordedReason.DETERMINATION_OF_MEANS);
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
            CaseDataTemplates.set(template, "activeJudgment", recordedJudgmentActiveJudgment());
        });
    }

    public static CaseData editJudgmentNoActiveJudgment() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joOrderMadeDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joPaymentPlan", new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
            CaseDataTemplates.set(template, "activeJudgment", null);
        });
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
            .setPaymentPlan(new JudgmentPaymentPlan()
                                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }

    private static JudgmentDetails recordedJudgmentActiveJudgment() {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.JUDGMENT_BY_COURT)
            .setIssueDate(LocalDate.now().minusDays(10))
            .setIsRegisterWithRTL(YesOrNo.NO)
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan()
                                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }
}
