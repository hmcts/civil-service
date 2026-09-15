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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class JudgmentPaidInFullFixtures {

    private JudgmentPaidInFullFixtures() {
    }

    public static CaseData markPaidInFull() {
        return basePaidInFullCase(LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(20))
            .build();
    }

    public static CaseData markPaidInFullWithFutureDate() {
        return basePaidInFullCase(LocalDate.now().plusDays(5), LocalDateTime.now().minusDays(20))
            .build();
    }

    public static CaseData markPaidInFullBeforeJudgmentDate() {
        return basePaidInFullCase(LocalDate.now().minusDays(10), LocalDateTime.now().minusDays(5))
            .activeJudgment(activeJudgment(LocalDate.now().minusDays(5)))
            .build();
    }

    private static CaseData.CaseDataBuilder<?, ?> basePaidInFullCase(LocalDate paymentDate, LocalDateTime createdDate) {
        return CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .joIsLiveJudgmentExists(YesOrNo.YES)
            .joDJCreatedDate(createdDate)
            .joJudgmentPaidInFull(paidInFull(paymentDate))
            .activeJudgment(activeJudgment(createdDate.toLocalDate()));
    }

    private static JudgmentPaidInFull paidInFull(LocalDate date) {
        JudgmentPaidInFull paidInFull = new JudgmentPaidInFull();
        paidInFull.setDateOfFullPaymentMade(date);
        return paidInFull;
    }

    private static JudgmentDetails activeJudgment(LocalDate issueDate) {
        return new JudgmentDetails()
            .setState(JudgmentState.ISSUED)
            .setType(JudgmentType.DEFAULT_JUDGMENT)
            .setIssueDate(issueDate)
            .setIsRegisterWithRTL(YesOrNo.YES)
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setOrderedAmount("100000")
            .setCosts("10200")
            .setTotalAmount("110200")
            .setPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY));
    }
}
