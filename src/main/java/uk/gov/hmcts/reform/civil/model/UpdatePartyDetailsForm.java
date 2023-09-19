package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class UpdatePartyDetailsForm {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String partyId;
    private String fieldOfExpertise;
}
