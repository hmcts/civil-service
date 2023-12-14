package uk.gov.hmcts.reform.civil.assertion;

import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;

import java.util.Optional;

public class RoboticsAddressAssert extends CustomAssert<RoboticsAddressAssert, RoboticsAddress> {

    public RoboticsAddressAssert(RoboticsAddress actual) {
        super("RoboticsAddress", actual, RoboticsAddressAssert.class);
    }

    public RoboticsAddressAssert isEqualTo(Address expected) {
        isNotNull();

        compare(
            "addressLine1",
            expected.firstNonNull(),
            Optional.ofNullable(actual.getAddressLine1())
        );

        compare(
            "addressLine2",
            Optional.ofNullable(expected.secondNonNull()).orElse("-"),
            Optional.ofNullable(actual.getAddressLine2())
        );

        compare(
            "addressLine3",
            expected.thirdNonNull(),
            Optional.ofNullable(actual.getAddressLine3())
        );

        compare(
            "addressLine4",
            expected.fourthNonNull(),
            Optional.ofNullable(actual.getAddressLine4())
        );

        compare(
            "addressLine5",
            expected.fifthNonNull(),
            Optional.ofNullable(actual.getAddressLine5())
        );

        compare(
            "postcode",
            expected.getPostCode(),
            Optional.ofNullable(actual.getPostCode())
        );

        return this;
    }

}
