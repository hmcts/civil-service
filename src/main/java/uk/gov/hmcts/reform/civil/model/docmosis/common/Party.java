package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

@Data
@Builder(toBuilder = true)
public class Party {

    private final String type;
    private final String name;
    private final String phoneNumber;
    private final String emailAddress;
    private final Address primaryAddress;
    private final Representative representative;
    private final String litigationFriendName;
    private final String litigationFriendFirstName;
    private final String litigationFriendLastName;
    private final String soleTraderTradingAs;
    private final String litigationFriendPhoneNumber;
    private final String litigationFriendEmailAddress;
    private final String legalRepHeading;

    public static Party toLipParty(uk.gov.hmcts.reform.civil.model.Party party) {
        return Party.builder()
            .name(party.getPartyName())
            .emailAddress(party.getPartyEmail())
            .type(party.getType().getDisplayValue())
            .phoneNumber(party.getPartyPhone())
            .primaryAddress(party.getPrimaryAddress())
            .build();
    }
}
