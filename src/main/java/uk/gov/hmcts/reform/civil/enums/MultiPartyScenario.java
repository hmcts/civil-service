package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public enum MultiPartyScenario {
    ONE_V_ONE,
    ONE_V_TWO_ONE_LEGAL_REP,
    TWO_V_ONE,
    ONE_V_TWO_TWO_LEGAL_REP;

    public static MultiPartyScenario getMultiPartyScenario(CaseData caseData) {
        if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES)) {
            return TWO_V_ONE;
        }

        if (caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative().equals(NO)) {
            return ONE_V_TWO_TWO_LEGAL_REP;
        } else if (caseData.getRespondent2SameLegalRepresentative() != null) {
            return ONE_V_TWO_ONE_LEGAL_REP;
        }

        return ONE_V_ONE;
    }
}
