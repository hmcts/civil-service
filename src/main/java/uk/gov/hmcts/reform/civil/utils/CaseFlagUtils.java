package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.List;

public class CaseFlagUtils {

    private CaseFlagUtils() {
        //NO-OP
    }

    public static Flags createFlags(String flagsPartyName, String roleOnCase) {
        return Flags.builder()
            .partyName(flagsPartyName)
            .roleOnCase(roleOnCase)
            .details(List.of())
            .build();
    }

    public static Party updateParty(String roleOnCase, Party partyToUpdate) {
        return partyToUpdate != null ? partyToUpdate.getFlags() != null ? partyToUpdate :
            partyToUpdate.toBuilder().flags(createFlags(partyToUpdate.getPartyName(), roleOnCase)).build() : null;
    }

    public static LitigationFriend updateLitFriend(String roleOnCase, LitigationFriend litFriendToUpdate) {
        return litFriendToUpdate != null ? litFriendToUpdate.getFlags() != null ? litFriendToUpdate
            : litFriendToUpdate.toBuilder().flags(createFlags(
                // LitigationFriend was updated to split fullName into firstname and lastname for H&L =================
                // ToDo: Remove the use of fullName after H&L changes are default =====================================
                litFriendToUpdate.getFullName() != null ? litFriendToUpdate.getFullName()
                    // ====================================================================================================
                    : String.format("%s %s", litFriendToUpdate.getFirstName(), litFriendToUpdate.getLastName()),
            roleOnCase)).build() : null;
    }

}
