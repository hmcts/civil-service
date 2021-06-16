package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class SolicitorServiceAddress {

    private final YesOrNo hasServiceAddress;
    private final Address address;
}
