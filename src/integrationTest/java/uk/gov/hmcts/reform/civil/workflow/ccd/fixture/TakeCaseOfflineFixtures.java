package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class TakeCaseOfflineFixtures {

    private static final String CLAIM_ISSUED = "claim-issued";

    private TakeCaseOfflineFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED);
    }

    public static CaseData lipCaseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "ccdState", CaseState.CASE_ISSUED);
        });
    }

    public static CaseData lipCaseDataBefore() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "ccdState", CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        });
    }
}
