package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Witness {

    @CCD(label = " ", showCondition = "phoneNumber = \"DO NOT SHOW\"", searchable = false, retainHiddenValue = true)
    private String partyID;
    @CCD(label = "Name", searchable = false)
    @Deprecated
    private String name;
    @CCD(label = "First name", searchable = false, max = 40)
    private String firstName;
    @CCD(label = "Last name", searchable = false, max = 40)
    private String lastName;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String phoneNumber;
    @CCD(label = "What event did they witness?", searchable = false)
    private String reasonForWitness;
    @CCD(label = "Event added", searchable = false)
    private String eventAdded;
    @CCD(label = "Date added", searchable = false)
    private LocalDate dateAdded;

    public Witness copy() {
        return new Witness()
            .setPartyID(partyID)
            .setName(name)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmailAddress(emailAddress)
            .setPhoneNumber(phoneNumber)
            .setReasonForWitness(reasonForWitness)
            .setEventAdded(eventAdded)
            .setDateAdded(dateAdded);
    }
}
