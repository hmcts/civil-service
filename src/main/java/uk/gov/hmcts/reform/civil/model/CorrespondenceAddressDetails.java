package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CorrespondenceAddressDetails {

    private final String email;
    private final String organisationName;
    private final String fax;
    private final String dx;
    private final String phoneNumber;
    private final Address address;
}
