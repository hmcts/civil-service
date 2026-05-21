package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class CreateLipClaimFixtures {

    private static final String PENDING_CLAIM_ISSUED = "pending-claim-issued";

    private CreateLipClaimFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(PENDING_CLAIM_ISSUED);
    }
}
