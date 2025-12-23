package uk.gov.hmcts.reform.civil;

import uk.gov.hmcts.reform.civil.enums.CaseState;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;

public class CaseDefinitionConstants {

    private CaseDefinitionConstants() {
        //NO-OP
    }

    public static final String JURISDICTION = "CIVIL";
    public static final String CASE_TYPE = "CIVIL";
    public static final String GENERALAPPLICATION_CASE_TYPE = "GENERALAPPLICATION";
    public static final String CMC_JURISDICTION = "CMC";
    public static final String CMC_CASE_TYPE = "MoneyClaimCase";
    public static final List<CaseState> NON_LIVE_STATES =
        List.of(APPLICATION_CLOSED, PROCEEDS_IN_HERITAGE, ORDER_MADE, APPLICATION_DISMISSED);
}
