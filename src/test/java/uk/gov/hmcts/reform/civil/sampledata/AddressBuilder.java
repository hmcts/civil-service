package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.Address;

import static com.google.common.base.Strings.repeat;

public class AddressBuilder {

    public static int MAX_ALLOWED = 35;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String postCode;
    private String postTown;
    private String county;
    private String country;

    public static AddressBuilder defaults() {
        return new AddressBuilder()
            .addressLine1("address line 1")
            .addressLine2("address line 2")
            .addressLine3("address line 3")
            .postCode("SW1 1AA")
            .postTown("London")
            .county("London")
            .country("UK");
    }

    public static AddressBuilder minimal() {
        return new AddressBuilder()
            .addressLine1("a");
    }

    public static AddressBuilder maximal() {
        return new AddressBuilder()
            .addressLine1(repeat("a", MAX_ALLOWED))
            .addressLine2(repeat("b", MAX_ALLOWED))
            .addressLine3(repeat("c", MAX_ALLOWED))
            .postCode("SW1 1AA")
            .county("London")
            .country("UNITED KINGDOM");
    }

    public AddressBuilder addressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public AddressBuilder addressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public AddressBuilder addressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
        return this;
    }

    public AddressBuilder postCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public AddressBuilder postTown(String postTown) {
        this.postTown = postTown;
        return this;
    }

    public AddressBuilder county(String county) {
        this.county = county;
        return this;
    }

    public AddressBuilder country(String country) {
        this.country = country;
        return this;
    }

    public Address build() {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostCode(postCode);
        address.setPostTown(postTown);
        address.setCounty(county);
        address.setCountry(country);
        return address;
    }
}
