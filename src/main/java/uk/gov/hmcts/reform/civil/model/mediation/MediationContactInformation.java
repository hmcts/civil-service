package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MediationContactInformation {

    @CCD(label = "First name", searchable = false, retainHiddenValue = true)
    private String firstName;
    @CCD(label = "Last name", searchable = false, retainHiddenValue = true)
    private String lastName;
    @CCD(label = "Email address", searchable = false, retainHiddenValue = true, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = "Telephone number", searchable = false, retainHiddenValue = true, typeOverride = FieldType.PhoneUK)
    private String telephoneNumber;

    @JsonCreator
    public MediationContactInformation(@JsonProperty("firstName") String firstName,
                                       @JsonProperty("lastName") String lastName,
                                       @JsonProperty("emailAddress") String emailAddress,
                                       @JsonProperty("telephoneNumber") String telephoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.telephoneNumber = telephoneNumber;
    }
}
