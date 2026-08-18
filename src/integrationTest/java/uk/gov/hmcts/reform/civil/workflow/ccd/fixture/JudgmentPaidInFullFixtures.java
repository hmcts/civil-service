package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class JudgmentPaidInFullFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";

    private JudgmentPaidInFullFixtures() {
    }

    public static CaseData markPaidInFull() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joDJCreatedDate", LocalDateTime.now().minusDays(20));
            CaseDataTemplates.set(template, "joJudgmentPaidInFull", paidInFull(LocalDate.now().minusDays(1)));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData markPaidInFullWithFutureDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joDJCreatedDate", LocalDateTime.now().minusDays(20));
            CaseDataTemplates.set(template, "joJudgmentPaidInFull", paidInFull(LocalDate.now().plusDays(5)));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData markPaidInFullBeforeJudgmentDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joDJCreatedDate", LocalDateTime.now().minusDays(5));
            CaseDataTemplates.set(template, "joJudgmentPaidInFull", paidInFull(LocalDate.now().minusDays(10)));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    private static JudgmentPaidInFull paidInFull(LocalDate date) {
        JudgmentPaidInFull paidInFull = new JudgmentPaidInFull();
        paidInFull.setDateOfFullPaymentMade(date);
        return paidInFull;
    }

    private static JudgmentDetails activeJudgment() {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setIssueDate(LocalDate.now().minusDays(20))
            .setIsRegisterWithRTL(YesOrNo.YES)
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan()
                                .setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }
}
