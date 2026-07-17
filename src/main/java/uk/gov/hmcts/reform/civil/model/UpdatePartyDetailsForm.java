package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdatePartyDetailsForm {

    @CCD(label = "First name", searchable = false)
    private String firstName;
    @CCD(label = "Last name", searchable = false)
    private String lastName;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String phoneNumber;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = " ", showCondition = "firstName = \"DO_NOT_SHOW_ON_UI\"", searchable = false)
    private String partyId;
    @CCD(label = "Field of expertise", searchable = false)
    private String fieldOfExpertise;
}
