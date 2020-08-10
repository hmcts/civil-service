package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.ServiceLocationType;

@Data
@Builder
public class ServiceLocation {

    private ServiceLocationType location;
    private String other;
}
