package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class LitigationFriend {

    private String partyID;

    // CIV-5557 to be removed
    private String fullName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private YesOrNo hasSameAddressAsLitigant;
    private Address primaryAddress;
    private List<Element<DocumentWithRegex>> certificateOfSuitability;
    private Flags flags;

    public LitigationFriend copy() {
        return new LitigationFriend()
            .setPartyID(partyID)
            .setFullName(fullName)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmailAddress(emailAddress)
            .setPhoneNumber(phoneNumber)
            .setHasSameAddressAsLitigant(hasSameAddressAsLitigant)
            .setPrimaryAddress(primaryAddress)
            .setCertificateOfSuitability(certificateOfSuitability)
            .setFlags(flags);
    }
}
