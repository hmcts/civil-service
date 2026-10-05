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

    public static CaseData pendingCaseIssuedData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.PENDING_CASE_ISSUED)
        );
    }

    public static CaseData caseDismissedData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.CASE_DISMISSED)
        );
    }

    public static CaseData awaitingRespondentAcknowledgementData() {
        return CaseDataTemplates.load(CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
        );
    }
}
