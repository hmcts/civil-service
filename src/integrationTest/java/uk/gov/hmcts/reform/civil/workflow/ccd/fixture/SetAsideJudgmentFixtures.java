package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;

public final class SetAsideJudgmentFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";

    private SetAsideJudgmentFixtures() {
    }

    public static CaseData setAsideJudgmentError() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joSetAsideReason", JudgmentSetAsideReason.JUDGMENT_ERROR);
            CaseDataTemplates.set(template, "joSetAsideOrderDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData setAsideJudgeOrderAfterApplication() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joSetAsideReason", JudgmentSetAsideReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joSetAsideOrderType", JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            CaseDataTemplates.set(template, "joSetAsideOrderDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joSetAsideApplicationDate", LocalDate.now().minusDays(10));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData setAsideJudgeOrderAfterDefence() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joSetAsideReason", JudgmentSetAsideReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joSetAsideOrderType", JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            CaseDataTemplates.set(template, "joSetAsideOrderDate", LocalDate.now().minusDays(5));
            CaseDataTemplates.set(template, "joSetAsideDefenceReceivedDate", LocalDate.now().minusDays(7));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData setAsideWithFutureOrderDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joSetAsideReason", JudgmentSetAsideReason.JUDGMENT_ERROR);
            CaseDataTemplates.set(template, "joSetAsideOrderDate", LocalDate.now().plusDays(5));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
    }

    public static CaseData setAsideApplicationDateAfterOrderDate() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "joIsLiveJudgmentExists", YesOrNo.YES);
            CaseDataTemplates.set(template, "joSetAsideReason", JudgmentSetAsideReason.JUDGE_ORDER);
            CaseDataTemplates.set(template, "joSetAsideOrderType", JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            CaseDataTemplates.set(template, "joSetAsideOrderDate", LocalDate.now().minusDays(10));
            CaseDataTemplates.set(template, "joSetAsideApplicationDate", LocalDate.now().minusDays(3));
            CaseDataTemplates.set(template, "activeJudgment", activeJudgment());
        });
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
            .setTotalAmount("110200");
    }
}
