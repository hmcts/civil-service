package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.Address;

import static com.google.common.base.Strings.repeat;

public class AddressBuilder {

    public static int MAX_ALLOWED = 35;

    public static Address.AddressBuilder defaults() {
        return Address.builder()
            .addressLine1("address line 1")
            .addressLine2("address line 2")
            .addressLine3("address line 3")
            .postCode("SW1 1AA")
            .county("London")
            .country("UK");
    }

    public static Address.AddressBuilder minimal() {
        return Address.builder()
            .addressLine1("a");
    }

    public static Address.AddressBuilder maximal() {
        return Address.builder()
            .addressLine1(repeat("a", MAX_ALLOWED))
            .addressLine2(repeat("b", MAX_ALLOWED))
            .addressLine3(repeat("c", MAX_ALLOWED))
            .postCode("SW1 1AA")
            .county("London")
            .country("UNITED KINGDOM");
    }

    public Address build() {
        return defaults().build();
    }
}
