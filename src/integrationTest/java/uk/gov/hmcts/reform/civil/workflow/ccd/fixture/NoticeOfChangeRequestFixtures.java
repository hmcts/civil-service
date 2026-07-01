package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class NoticeOfChangeRequestFixtures {

    private static final String CLAIM_ISSUED = "claim-issued";

    private NoticeOfChangeRequestFixtures() {
    }

    public static CaseData validCaseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.CASE_ISSUED)
        );
    }

    public static CaseData invalidCaseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
        );
    }
}
