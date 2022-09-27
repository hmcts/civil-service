package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.prd.model.ContactInformation;

import java.util.List;
import java.util.Optional;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

@Component
public class RoboticsAddressMapper {

    public RoboticsAddress toRoboticsAddress(Address address) {
        requireNonNull(address);
        return RoboticsAddress.builder()
            .addressLine1(address.firstNonNull())
            .addressLine2(Optional.ofNullable(address.secondNonNull()).orElse("-"))
            .addressLine3(address.thirdNonNull())
            .addressLine4(address.fourthNonNull())
            .addressLine5(address.fifthNonNull())
            .postCode(address.getPostCode())
            .build();
    }

    public RoboticsAddresses toRoboticsAddresses(Address address) {
        requireNonNull(address);
        return RoboticsAddresses.builder()
            .contactAddress(toRoboticsAddress(address))
            .build();
    }

    public RoboticsAddresses toRoboticsAddresses(List<ContactInformation> contactInformation) {
        return isEmpty(contactInformation)
            ? null
            : toRoboticsAddresses(fromContactInformation(contactInformation.get(0)));
    }
}
