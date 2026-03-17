package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdatePartyDetailsForm {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String partyId;
    private String fieldOfExpertise;
}
