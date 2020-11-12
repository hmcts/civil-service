package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddresses;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

@Component
public class RoboticsAddressMapper {

    public RoboticsAddress toRoboticsAddress(Address address) {
        requireNonNull(address);
        return RoboticsAddress.builder()
            .addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine2())
            .addressLine3(address.getAddressLine3())
            .addressLine4(join(", ", address.getPostTown(), address.getCounty()))
            .addressLine5(address.getCountry())
            .postCode(address.getPostCode())
            .build();
    }

    public RoboticsAddresses toRoboticsAddresses(Address address) {
        requireNonNull(address);
        return RoboticsAddresses.builder()
            .contactAddress(toRoboticsAddress(address))
            .build();
    }
}
