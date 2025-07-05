package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;

import java.util.Objects;

public class AddressUtils {

    private AddressUtils() {
        //no op
    }

    public static String formatAddress(Address address) {
        StringBuilder formattedLine = new StringBuilder();
        formattedLine.append(formatAddressLine(address.getAddressLine1()))
            .append(formatAddressLine(address.getAddressLine2()))
            .append(formatAddressLine(address.getAddressLine3()))
            .append(formatAddressLine(address.getCounty()))
            .append(formatAddressLine(address.getPostTown()))
            .append(formatAddressLine(address.getPostCode()))
            .append(formatAddressLine(address.getCountry()));
        String result = formattedLine.toString().trim();
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    private static String formatAddressLine(String line) {
        return line != null ? line + ", " : "";
    }

    public static Address getAddress(ContactInformation address) {
        return Address.builder().addressLine1(address.getAddressLine1())
            .addressLine2(Objects.toString(address.getAddressLine2(), ""))
            .addressLine3(Objects.toString(address.getAddressLine3(), ""))
            .country(address.getCountry())
            .county(address.getCounty())
            .postCode(address.getPostCode())
            .postTown(address.getTownCity())
            .build();
    }
}
