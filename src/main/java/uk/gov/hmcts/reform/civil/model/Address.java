package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.utils.ObjectUtils;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;

import static uk.gov.hmcts.reform.civil.utils.StringUtils.joinNonNull;

@Data
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String postTown;
    private String county;
    private String country;
    private String postCode;

    @JsonIgnore
    public String firstNonNull() {
        return ObjectUtils.firstNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            joinNonNull(", ", county, country)
        );
    }

    @JsonIgnore
    public String secondNonNull() {
        return ObjectUtils.secondNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            joinNonNull(", ", county, country)
        );
    }

    @JsonIgnore
    public String thirdNonNull() {
        return ObjectUtils.thirdNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            joinNonNull(", ", county, country)
        );
    }

    @JsonIgnore
    public String fourthNonNull() {
        return ObjectUtils.fourthNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            joinNonNull(", ", county, country)
        );
    }

    @JsonIgnore
    public String fifthNonNull() {
        return ObjectUtils.fifthNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            joinNonNull(", ", county, country)
        );
    }

    @JsonIgnore
    public static Address fromContactInformation(ContactInformation contactInformation) {
        return Address.builder()
            .addressLine1(contactInformation.getAddressLine1())
            .addressLine2(contactInformation.getAddressLine2())
            .addressLine3(contactInformation.getAddressLine3())
            .postTown(contactInformation.getTownCity())
            .county(contactInformation.getCounty())
            .country(contactInformation.getCountry())
            .postCode(contactInformation.getPostCode())
            .build();
    }
}
