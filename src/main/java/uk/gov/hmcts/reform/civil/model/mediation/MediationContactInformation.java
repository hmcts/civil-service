package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MediationContactInformation {

    private String firstName;
    private String lastName;
    private String emailAddress;
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
