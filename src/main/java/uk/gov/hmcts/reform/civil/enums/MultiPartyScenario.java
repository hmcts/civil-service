package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public enum MultiPartyScenario {
    ONE_V_ONE,
    /**
     * one claimant vs two defendants with one LR for both defendants.
     */
    ONE_V_TWO_SAME_LEGAL_REP,
    /**
     * two claimants vs one defendant.
     */
    TWO_V_ONE,
    /**
     * one claimant vs two defendants with one LR for each defendant.
     */
    ONE_V_TWO_TWO_LEGAL_REP,

    ONE_V_TWO_ONE_LEGAL_REP;

    public static MultiPartyScenario getMultiPartyScenario(CaseData caseData) {
        if (has2Applicants(caseData)) {
            return TWO_V_ONE;
        } else if (has2Respondents(caseData)) {
            if (YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
                return ONE_V_TWO_SAME_LEGAL_REP;
            } else if (has2DifferentLegalRepresentatives(caseData)) {
                return ONE_V_TWO_TWO_LEGAL_REP;
            } else {
                return ONE_V_TWO_ONE_LEGAL_REP;
            }
        } else {
            return ONE_V_ONE;
        }
    }

    private static boolean has2DifferentLegalRepresentatives(CaseData caseData) {
        return YES.equals(caseData.getRespondent1Represented())
            && YES.equals(caseData.getRespondent2Represented());
    }

    private static boolean has2Applicants(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES);
    }

    private static boolean has2Respondents(CaseData caseData) {
        return caseData.getRespondent2() != null || caseData.getRespondent2Copy() != null;
    }

    public static boolean isMultiPartyScenario(CaseData caseData) {
        return caseData.getApplicant2() != null || caseData.getRespondent2() != null;
    }

    public static boolean isOneVOne(CaseData caseData) {
        return ONE_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static boolean isOneVTwoLegalRep(CaseData caseData) {
        return ONE_V_TWO_SAME_LEGAL_REP.equals(getMultiPartyScenario(caseData));
    }

    public static boolean isTwoVOne(CaseData caseData) {
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static boolean isOneVTwoTwoLegalRep(CaseData caseData) {
        return ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData));
    }
}
