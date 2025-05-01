package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartyDetailsForm {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String partyId;
    private String fieldOfExpertise;
}
