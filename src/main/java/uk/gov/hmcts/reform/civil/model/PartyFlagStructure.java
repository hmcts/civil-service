package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Accessors(chain = true)
public class PartyFlagStructure {

    @CCD(label = " ", showCondition = "phone = \"DO NOT SHOW\"", searchable = false)
    private String partyID;
    @CCD(label = "First name", searchable = false, retainHiddenValue = true)
    private String firstName;
    @CCD(label = "Last name", searchable = false, retainHiddenValue = true)
    private String lastName;
    @CCD(label = "Email", searchable = false, retainHiddenValue = true, typeOverride = FieldType.Email)
    private String email;
    @CCD(label = "Phone", searchable = false, retainHiddenValue = true)
    private String phone;
    @CCD(label = "flags", searchable = false, retainHiddenValue = true)
    private Flags flags;

    public PartyFlagStructure copy() {
        return new PartyFlagStructure()
            .setPartyID(partyID)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmail(email)
            .setPhone(phone)
            .setFlags(flags);
    }
}
