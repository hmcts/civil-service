package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;

import java.util.List;
import java.util.Optional;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

@Service
@RequiredArgsConstructor
public class RoboticsAddressMapper {

    private final AddressLinesMapper addressLinesMapper;

    public RoboticsAddress toRoboticsAddress(Address originalAddress) {
        requireNonNull(originalAddress);
        Address address = addressLinesMapper.splitLongerLines(originalAddress);
        return new RoboticsAddress()
            .setAddressLine1(address.firstNonNull())
            .setAddressLine2(Optional.ofNullable(address.secondNonNull()).orElse("-"))
            .setAddressLine3(address.thirdNonNull())
            .setAddressLine4(address.fourthNonNull())
            .setAddressLine5(address.fifthNonNull())
            .setPostCode(address.getPostCode());
    }

    public RoboticsAddresses toRoboticsAddresses(Address address) {
        requireNonNull(address);
        return new RoboticsAddresses()
            .setContactAddress(toRoboticsAddress(address));
    }

    public RoboticsAddresses toRoboticsAddresses(List<ContactInformation> contactInformation) {
        return isEmpty(contactInformation)
            ? null
            : toRoboticsAddresses(fromContactInformation(contactInformation.get(0)));
    }
}
