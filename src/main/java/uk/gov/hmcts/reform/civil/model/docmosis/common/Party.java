package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

@Data
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

    public Party copy() {
        return new Party()
            .setType(this.type)
            .setName(this.name)
            .setPhoneNumber(this.phoneNumber)
            .setEmailAddress(this.emailAddress)
            .setPrimaryAddress(this.primaryAddress)
            .setRepresentative(this.representative)
            .setLitigationFriendName(this.litigationFriendName)
            .setLitigationFriendFirstName(this.litigationFriendFirstName)
            .setLitigationFriendLastName(this.litigationFriendLastName)
            .setSoleTraderTradingAs(this.soleTraderTradingAs)
            .setLitigationFriendPhoneNumber(this.litigationFriendPhoneNumber)
            .setLitigationFriendEmailAddress(this.litigationFriendEmailAddress)
            .setLegalRepHeading(this.legalRepHeading);
    }
}
