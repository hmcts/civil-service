package uk.gov.hmcts.reform.civil.model.robotics;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoboticsAddresses {

    private RoboticsAddress contactAddress;
}
