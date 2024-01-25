package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressUtilsTest {

    @Test
    public void testFormatAddress() {
        Address address = Address.builder()
            .addressLine1("123 Street")
            .addressLine2("Apartment 4B")
            .postTown("City")
            .postCode("12345")
            .country("Country")
            .build();

        String formattedAddress = AddressUtils.formatAddress(address);

        String expectedFormattedAddress = "123 Street, Apartment 4B, City, 12345, Country";
        assertEquals(expectedFormattedAddress, formattedAddress);
    }

    @Test
    public void testFormatAddressWithNullFields() {
        Address address = Address.builder()
            .addressLine1("123 Street")
            .country("Country")
            .build();

        String formattedAddress = AddressUtils.formatAddress(address);

        String expectedFormattedAddress = "123 Street, Country";
        assertEquals(expectedFormattedAddress, formattedAddress);
    }

    @Test
    public void testFormatEmptyAddress() {
        Address address = Address.builder().build();

        String formattedAddress = AddressUtils.formatAddress(address);

        assertEquals("", formattedAddress);
    }
}
