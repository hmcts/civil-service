package uk.gov.hmcts.reform.civil.workflow.dashboard.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class OrderMadeClaimantFixtures {

    private static final String ORDER_MADE_CLAIMANT = "order-made-claimant";
    private static final long CASE_ID = 7201458805332L;

    private OrderMadeClaimantFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(ORDER_MADE_CLAIMANT, template -> {
            CaseDataTemplates.set(template, "ccdCaseReference", CASE_ID);
            CaseDataTemplates.set(template, "ccdState", CaseState.All_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "claimsTrack", ClaimsTrack.FAST_TRACK);
            CaseDataTemplates.set(template, "drawDirectionsOrderRequired", YesOrNo.NO);
            CaseDataTemplates.set(template, "trialReadyApplicant", null);
        });
    }

    public static String caseReference() {
        return Long.toString(CASE_ID);
    }

}
