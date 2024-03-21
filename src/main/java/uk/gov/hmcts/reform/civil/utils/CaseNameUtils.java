package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;

public class CaseNameUtils {

    private CaseNameUtils() {
        //no op
    }

    public static String buildCaseName(CaseData caseData) {
        return new StringBuilder()
            .append(getFormattedPartyName(caseData.getApplicant1()))
            .append(getFormattedLitigationFriendName(caseData.getApplicant1LitigationFriend()))
            .append(getFormattedPartyName(caseData.getApplicant2(), true))
            .append(getFormattedLitigationFriendName(caseData.getApplicant2LitigationFriend()))
            .append(" v ")
            .append(getFormattedPartyName(caseData.getRespondent1()))
            .append(getFormattedLitigationFriendName(caseData.getRespondent1LitigationFriend()))
            .append(getFormattedPartyName(caseData.getRespondent2(), true))
            .append(getFormattedLitigationFriendName(caseData.getRespondent2LitigationFriend()))
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
}
