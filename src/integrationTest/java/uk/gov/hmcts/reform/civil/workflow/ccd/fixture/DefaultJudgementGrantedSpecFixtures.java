package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class DefaultJudgementGrantedSpecFixtures {

    private static final String CLAIM_ISSUED_TEMPLATE = "claim-issued";

    private DefaultJudgementGrantedSpecFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED_TEMPLATE, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.JUDGMENT_REQUESTED);
            CaseDataTemplates.set(template, "activeJudgment", new JudgmentDetails().setState(JudgmentState.ISSUED));
            CaseDataTemplates.set(template, "ccdCaseReference", 1234567890123456L);
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
