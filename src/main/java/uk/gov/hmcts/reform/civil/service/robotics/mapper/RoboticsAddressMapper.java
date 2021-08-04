package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.service.robotics.exception.AddressLineExceedsLengthLimitException;
import uk.gov.hmcts.reform.prd.model.ContactInformation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

@Component
public class RoboticsAddressMapper {

    public RoboticsAddress toRoboticsAddress(Address address) {
        requireNonNull(address);

        if ((address.getAddressLine1() != null && address.getAddressLine1().length() > 35)
            || (address.getAddressLine2() != null && address.getAddressLine2().length() > 35)) {
            try {
                Address alteredAddress = tryToRollOverAddressLines(address);
                return toNonDefaultRoboticsAddress(alteredAddress);
            } catch (AddressLineExceedsLengthLimitException ignored) {
                return toDefaultRoboticsAddress(address);
            }
        }

        return toDefaultRoboticsAddress(address);
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

    private Address tryToRollOverAddressLines(Address address) {
        String[] addressLines = new String[]{
            address.getAddressLine1(), address.getAddressLine2(), address.getAddressLine3()};

        if (addressLines[0].length() > 35) {
            tryToRollOverAddressLine(addressLines, 0);
        }

        if (addressLines[1] != null && addressLines[1].length() > 35) {
            tryToRollOverAddressLine(addressLines, 1);
        }

        if (Arrays.stream(addressLines).filter(Objects::nonNull).anyMatch(addressLine -> addressLine.length() > 35)) {
            throw new AddressLineExceedsLengthLimitException();
        }

        return Address.builder()
            .addressLine1(addressLines[0])
            .addressLine2(addressLines[1])
            .addressLine3(addressLines[2])
            .postTown(address.getPostTown())
            .county(address.getCounty())
            .country(address.getCountry())
            .postCode(address.getPostCode())
            .build();
    }

    private void tryToRollOverAddressLine(String[] addressLines, int addressLineIndex) {
        String[] addressSplit = addressLines[addressLineIndex].split(", ");

        if (addressSplit.length == 1) {
            throw new AddressLineExceedsLengthLimitException();
        }

        addressLines[addressLineIndex] = String.join(
            ", ",
            ArrayUtils.subarray(addressSplit, 0, addressSplit.length - 1)
        );

        if (addressLines[addressLineIndex + 1] == null) {
            addressLines[addressLineIndex + 1] = addressSplit[addressSplit.length - 1];
        } else {
            addressLines[addressLineIndex + 1] = addressSplit[addressSplit.length - 1] + ", "
                + addressLines[addressLineIndex + 1];
        }
    }

    private RoboticsAddress toDefaultRoboticsAddress(Address address) {
        return RoboticsAddress.builder()
            .addressLine1(address.firstNonNull())
            .addressLine2(Optional.ofNullable(address.secondNonNull()).orElse("-"))
            .addressLine3(address.thirdNonNull())
            .addressLine4(address.fourthNonNull())
            .addressLine5(address.fifthNonNull())
            .postCode(address.getPostCode())
            .build();
    }

    private RoboticsAddress toNonDefaultRoboticsAddress(Address address) {
        return RoboticsAddress.builder()
            .addressLine1(address.firstNonNull())
            .addressLine2(address.secondNonNull())
            .addressLine3(address.thirdNonNull())
            .addressLine4(address.fourthNonNull())
            .addressLine5(address.fifthNonNull())
            .postCode(address.getPostCode())
            .build();
    }
}
