package uk.gov.hmcts.reform.unspec.assertion;

import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;

import java.util.Optional;

import static java.lang.String.join;

public class RoboticsAddressAssert extends CustomAssert<RoboticsAddressAssert, RoboticsAddress> {

    public RoboticsAddressAssert(RoboticsAddress actual) {
        super("RoboticsAddress", actual, RoboticsAddressAssert.class);
    }

    public RoboticsAddressAssert isEqualTo(Address expected) {
        isNotNull();

        compare(
            "addressLine1",
            expected.getAddressLine1(),
            Optional.ofNullable(actual.getAddressLine1())
        );

        compare(
            "addressLine2",
            expected.getAddressLine2(),
            Optional.ofNullable(actual.getAddressLine2())
        );

        compare(
            "addressLine3",
            expected.getAddressLine3(),
            Optional.ofNullable(actual.getAddressLine3())
        );

        compare(
            "addressLine4",
            join(", ", expected.getPostTown(), expected.getCounty()),
            Optional.ofNullable(actual.getAddressLine4())
        );

        compare(
            "addressLine5",
            expected.getCountry(),
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
