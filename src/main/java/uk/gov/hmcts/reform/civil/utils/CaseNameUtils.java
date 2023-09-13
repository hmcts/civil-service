package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

public class CaseNameUtils {

    private CaseNameUtils() {
        //no op
    }

    public static String buildCaseNamePublic(CaseData caseData) {
        return new StringBuilder()
            .append(getFormattedPartyName(caseData.getApplicant1()))
            .append(getFormattedLitigationFriendName(caseData.getApplicant1LitigationFriend()))
            .append(getFormattedPartyName(caseData.getApplicant2(), true))
            .append(getFormattedLitigationFriendName(caseData.getApplicant2LitigationFriend()))
            .append(" v ")
            .append(getFormattedPartyName(caseData.getRespondent1()))
            .append(getFormattedPartyName(caseData.getRespondent2(), true))
            .toString();
    }

    public static String getFormattedPartyName(Party party, boolean commaPrefix) {
        String prefix = commaPrefix ? ", " : "";
        return party != null ? String.format("%s'%s'", prefix, party.getPartyName(true)) : "";
    }

    public static String getFormattedPartyName(Party party) {
        return getFormattedPartyName(party, false);
    }

    public static String getFormattedLitigationFriendName(LitigationFriend litigationFriend) {
        return litigationFriend != null
            ? String.format(" represented by '%s %s' (litigation friend)",
                            litigationFriend.getFirstName(),
                            litigationFriend.getLastName()
        ) : "";
    }

    public static String buildCaseNameInternal(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ").append(caseData.getRespondent1().getPartyName())
                .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" and ").append(caseData.getApplicant2().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());
        }
        return participantString.toString();
    }
}
