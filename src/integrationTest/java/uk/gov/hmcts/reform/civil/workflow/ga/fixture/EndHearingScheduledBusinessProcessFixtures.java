package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class EndHearingScheduledBusinessProcessFixtures {

    private static final String HEARING_SCHEDULED = "ga/hearing-scheduled";

    private EndHearingScheduledBusinessProcessFixtures() {
    }

    public static GeneralApplicationCaseData caseData() {
        return CaseDataTemplates.load(HEARING_SCHEDULED, GeneralApplicationCaseData.class);
    }
}
