package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressUtilsTest {

    @Test
    void testFormatAddress() {
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
    void testFormatAddressWithNullFields() {
        Address address = Address.builder()
            .addressLine1("123 Street")
            .country("Country")
            .build();

        String formattedAddress = AddressUtils.formatAddress(address);

        String expectedFormattedAddress = "123 Street, Country";
        assertEquals(expectedFormattedAddress, formattedAddress);
    }

    @Test
    void testFormatEmptyAddress() {
        Address address = Address.builder().build();

        String formattedAddress = AddressUtils.formatAddress(address);

        assertEquals("", formattedAddress);
    }

    @Test
    void getAddress() {
        ContactInformation contactInformation = ContactInformation.builder()
            .addressLine1("Hello")
            .addressLine2("World")
            .addressLine3("AI")
            .county("MDX")
            .country("United Kingdom")
            .townCity("London")
            .postCode("SW19 2PQ")
            .build();

        Address address = AddressUtils.getAddress(contactInformation);
        assertEquals("Hello", address.getAddressLine1());
        assertEquals("World", address.getAddressLine2());
        assertEquals("AI", address.getAddressLine3());
        assertEquals("London", address.getPostTown());
        assertEquals("MDX", address.getCounty());
        assertEquals("United Kingdom", address.getCountry());
        assertEquals("SW19 2PQ", address.getPostCode());

        contactInformation = ContactInformation.builder()
            .addressLine1("Hello")
            .addressLine2(null)
            .addressLine3(null)
            .townCity("London")
            .county("MDX")
            .country("United Kingdom")
            .postCode("SW19 2PQ")
            .build();

        Address address1 = AddressUtils.getAddress(contactInformation);
        assertEquals("Hello", address1.getAddressLine1());
        assertEquals("", address1.getAddressLine2());
        assertEquals("", address1.getAddressLine3());
        assertEquals("MDX", address1.getCounty());
        assertEquals("London", address1.getPostTown());
        assertEquals("United Kingdom", address1.getCountry());
        assertEquals("SW19 2PQ", address1.getPostCode());
    }
}
