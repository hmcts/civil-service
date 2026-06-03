package uk.gov.hmcts.reform.civil.workflow.dashboard.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

public final class OrderMadeDefendantFixtures {

    private static final String ORDER_MADE_DEFENDANT = "order-made-defendant";
    private static final long CASE_ID = 725984154548966L;

    private OrderMadeDefendantFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(ORDER_MADE_DEFENDANT, template -> {
            CaseDataTemplates.set(template, "ccdCaseReference", CASE_ID);
            CaseDataTemplates.set(template, "ccdState", CaseState.ALL_FINAL_ORDERS_ISSUED);
            CaseDataTemplates.set(template, "claimsTrack", ClaimsTrack.FAST_TRACK);
            CaseDataTemplates.set(template, "drawDirectionsOrderRequired", YesOrNo.NO);
            CaseDataTemplates.set(template, "trialReadyRespondent1", null);
        });
    }

    public static String caseReference() {
        return String.valueOf(CASE_ID);
    }

}
