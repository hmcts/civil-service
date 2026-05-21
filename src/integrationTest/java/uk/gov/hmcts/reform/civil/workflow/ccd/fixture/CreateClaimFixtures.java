package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class CreateClaimFixtures {

    private static final String CREATE_CLAIM_START = "create-claim-start";

    private CreateClaimFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CREATE_CLAIM_START);
    }
}
