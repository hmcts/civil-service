package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class NotifyEventFixtures {

    private static final String CLAIM_DETAILS_NOTIFIED = "claim-details-notified";
    private static final String ACTIVITY_ID = "task-id";

    private NotifyEventFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CLAIM_DETAILS_NOTIFIED, template ->
            CaseDataTemplates.set(template, "businessProcess", new BusinessProcess().setActivityId(ACTIVITY_ID))
        );
    }

    public static String activityId() {
        return ACTIVITY_ID;
    }
}
