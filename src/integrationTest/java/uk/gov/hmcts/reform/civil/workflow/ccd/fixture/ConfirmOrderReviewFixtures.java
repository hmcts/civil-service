package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class ConfirmOrderReviewFixtures {

    private static final String TEMPLATE = "case-progression";

    private ConfirmOrderReviewFixtures() {
    }

    public static CaseData finalOrderFromDecisionOutcome() {
        return CaseDataTemplates.load(TEMPLATE).toBuilder()
            .ccdState(CaseState.DECISION_OUTCOME)
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .isFinalOrder(YesOrNo.YES)
            .build();
    }

    public static CaseData nonFinalOrderFromDecisionOutcome() {
        return CaseDataTemplates.load(TEMPLATE).toBuilder()
            .ccdState(CaseState.DECISION_OUTCOME)
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .isFinalOrder(YesOrNo.NO)
            .build();
    }
}
