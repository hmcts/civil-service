package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

public class NotificationUtils {

    public static Boolean isRespondent1(CallbackParams callbackParams, CaseEvent matchEvent) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(matchEvent);
    }

    public static boolean is1v1Or2v1Case(CaseData caseData) {
        return getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE);
    }

}
