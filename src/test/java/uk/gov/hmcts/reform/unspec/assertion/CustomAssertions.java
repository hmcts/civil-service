package uk.gov.hmcts.reform.unspec.assertion;

import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;

public class CustomAssertions {

    private CustomAssertions() {
        //utility class
    }

    public static RoboticsAddressAssert assertThat(RoboticsAddress roboticsAddress) {
        return new RoboticsAddressAssert(roboticsAddress);
    }

    public static RoboticsCaseDataAssert assertThat(RoboticsCaseData roboticsCaseData) {
        return new RoboticsCaseDataAssert(roboticsCaseData);
    }
}
