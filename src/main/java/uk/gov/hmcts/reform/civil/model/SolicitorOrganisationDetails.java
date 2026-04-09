package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SolicitorOrganisationDetails {

    private String email;
    private String organisationName;
    private String fax;
    private String dx;
    private String phoneNumber;
    private Address address;
}
