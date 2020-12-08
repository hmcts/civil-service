package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoboticsAddress {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String postCode;
}
