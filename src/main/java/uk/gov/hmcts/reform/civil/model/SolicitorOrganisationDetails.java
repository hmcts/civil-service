package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SolicitorOrganisationDetails {

    private String email;
    private String organisationName;
    private String fax;
    private String dx;
    private String phoneNumber;
    private Address address;
}
