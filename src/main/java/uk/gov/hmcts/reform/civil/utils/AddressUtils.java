package uk.gov.hmcts.reform.civil.utils;

import camundajar.impl.scala.collection.mutable.StringBuilder;
import uk.gov.hmcts.reform.civil.model.Address;

public class AddressUtils {

    private AddressUtils() {
        //no op
    }

    public static  String formatAddress(Address address) {
        String formattedLine = new StringBuilder()
            .addAll(formatAddressLine(address.getAddressLine1()))
            .addAll(formatAddressLine(address.getAddressLine2()))
            .addAll(formatAddressLine(address.getAddressLine3()))
            .addAll(formatAddressLine(address.getCounty()))
            .addAll(formatAddressLine(address.getPostTown()))
            .addAll(formatAddressLine(address.getPostCode()))
            .addAll(formatAddressLine(address.getCountry()))
            .result().trim();
        return formattedLine.length() > 0 ? formattedLine.substring(0, formattedLine.length() - 1) : "";
    }

    private static String formatAddressLine(String line) {
        return line != null ? line + ", " : "";
    }

}
