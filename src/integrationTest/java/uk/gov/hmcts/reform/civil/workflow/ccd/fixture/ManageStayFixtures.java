package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;

public final class ManageStayFixtures {

    private static final String CLAIM_ISSUED = "claim-issued";

    private ManageStayFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "ccdState", CaseState.CASE_STAYED);
            CaseDataTemplates.set(template, "preStayState", CaseState.DECISION_OUTCOME.name());
            CaseDataTemplates.set(template, "manageStayOption", "LIFT_STAY");
            CaseDataTemplates.set(template, "caseStayDate", LocalDate.of(2026, 5, 1));
        });
    }
}
