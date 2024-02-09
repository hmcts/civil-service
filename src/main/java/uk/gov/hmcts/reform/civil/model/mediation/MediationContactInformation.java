package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
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
