package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SolicitorOrganisationDetails {

    private final String email;
    private final String organisationName;
    private final String fax;
    private final String dx;
    private final String phoneNumber;
    private final Address address;
}
