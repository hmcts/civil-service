package uk.gov.hmcts.reform.migration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Party {
    private final String partyID;
    private final String idamID;
    private final String partyType;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final String organisationName;
    private final String dateOfBirth;
    private final Address address;
    private final EmailAddress email;
    private final TelephoneNumber telephoneNumber;
}
