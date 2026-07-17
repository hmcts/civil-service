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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class LitigationFriend {

    @CCD(
            label = " ",
            showCondition = "hasSameAddressAsLitigant = \"DO NOT SHOW\"",
            searchable = false,
            retainHiddenValue = true
    )
    private String partyID;

    // CIV-5557 to be removed
    @CCD(label = "Litigation friend's full name", searchable = false)
    private String fullName;
    @CCD(label = "First name", searchable = false)
    private String firstName;
    @CCD(label = "Last name", searchable = false)
    private String lastName;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String phoneNumber;
    @CCD(
            label = "Is the address of the Claimant’s litigation friend the same as the address of the Claimant?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo hasSameAddressAsLitigant;
    @CCD(label = "Litigation friend's address", showCondition = "hasSameAddressAsLitigant = \"No\"", searchable = false)
    private Address primaryAddress;
    @CCD(
            label = "Upload the certificate of suitability",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentOrImageWithRegex"
    )
    private List<Element<DocumentWithRegex>> certificateOfSuitability;
    @CCD(label = " ", searchable = false, retainHiddenValue = true)
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
