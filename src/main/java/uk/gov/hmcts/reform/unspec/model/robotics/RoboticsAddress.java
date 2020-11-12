package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoboticsAddress {

    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String addressLine4;
    private final String addressLine5;
    private final String postCode;
}
