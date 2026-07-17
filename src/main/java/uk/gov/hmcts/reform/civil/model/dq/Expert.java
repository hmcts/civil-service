package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Expert {

    @CCD(label = " ", showCondition = "phoneNumber = \"DO NOT SHOW\"", searchable = false, retainHiddenValue = true)
    private String partyID;
    @CCD(label = "Expert's name", searchable = false)
    private String name;
    @CCD(label = "First name", searchable = false, max = 40)
    private String firstName;
    @CCD(label = "Last name", searchable = false, max = 40)
    private String lastName;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String phoneNumber;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = "Field of expertise", searchable = false)
    private String fieldOfExpertise;
    @CCD(label = "Why do you need this expert?", searchable = false, typeOverride = FieldType.TextArea)
    private String whyRequired;
    @CCD(label = "Estimated cost", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;
    @CCD(label = "Event added", searchable = false)
    private String eventAdded;
    @CCD(label = "Date added", searchable = false)
    private LocalDate dateAdded;

    public static Expert fromSmallClaimExpertDetails(ExpertDetails expertDetails) {
        return new Expert()
            .setName(expertDetails.getExpertName())
            .setFirstName(expertDetails.getFirstName())
            .setLastName(expertDetails.getLastName())
            .setPhoneNumber(expertDetails.getPhoneNumber())
            .setEmailAddress(expertDetails.getEmailAddress())
            .setFieldOfExpertise(expertDetails.getFieldofExpertise())
            .setWhyRequired(expertDetails.getWhyRequired())
            .setEstimatedCost(expertDetails.getEstimatedCost());
    }

    public Expert copy() {
        return new Expert()
            .setPartyID(partyID)
            .setName(name)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setPhoneNumber(phoneNumber)
            .setEmailAddress(emailAddress)
            .setFieldOfExpertise(fieldOfExpertise)
            .setWhyRequired(whyRequired)
            .setEstimatedCost(estimatedCost)
            .setEventAdded(eventAdded)
            .setDateAdded(dateAdded);
    }
}
