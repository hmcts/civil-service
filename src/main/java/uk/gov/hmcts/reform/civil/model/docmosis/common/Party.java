package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Party {

    private String type;
    private String name;
    private String phoneNumber;
    private String emailAddress;
    private Address primaryAddress;
    private Representative representative;
    private String litigationFriendName;
    private String litigationFriendFirstName;
    private String litigationFriendLastName;
    private String soleTraderTradingAs;
    private String litigationFriendPhoneNumber;
    private String litigationFriendEmailAddress;
    private String legalRepHeading;

    public static Party toLipParty(uk.gov.hmcts.reform.civil.model.Party party) {
        return new Party()
            .setName(party.getPartyName())
            .setEmailAddress(party.getPartyEmail())
            .setType(party.getType().getDisplayValue())
            .setPhoneNumber(party.getPartyPhone())
            .setPrimaryAddress(party.getPrimaryAddress());
    }
}
