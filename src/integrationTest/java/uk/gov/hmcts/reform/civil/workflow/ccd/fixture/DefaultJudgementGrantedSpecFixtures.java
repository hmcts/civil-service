package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.math.BigDecimal;

public final class DefaultJudgementGrantedSpecFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";
    private static final String REPAYMENT_SUMMARY_OBJECT = "The judgment will order the defendant to pay £1172.00, "
        + "including the claim fee and interest, if applicable, as shown:\n### Claim amount \n £1000.00\n "
        + "### Fixed cost amount \n£102.00\n### Claim fee amount \n £70.00\n ## Subtotal \n £1172.00";

    private DefaultJudgementGrantedSpecFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.JUDGMENT_REQUESTED);
            CaseDataTemplates.set(template, "activeJudgment", new JudgmentDetails().setState(JudgmentState.ISSUED));
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
            CaseDataTemplates.set(template, "totalClaimAmount", BigDecimal.valueOf(1000));
            CaseDataTemplates.set(template, "repaymentSummaryObject", REPAYMENT_SUMMARY_OBJECT);
        });
    }

    public static CaseData caseDataWithNoActiveJudgment() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.JUDGMENT_REQUESTED);
            CaseDataTemplates.set(template, "activeJudgment", null);
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
        });
    }

    public static CaseData caseDataWithWrongState() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.CASE_ISSUED);
            CaseDataTemplates.set(template, "activeJudgment", new JudgmentDetails().setState(JudgmentState.ISSUED));
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
        });
    }
}
