package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.utils.ObjectUtils;

import static uk.gov.hmcts.reform.civil.utils.StringUtils.joinNonNull;

@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
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
        return new Address(
            contactInformation.getAddressLine1(),
            contactInformation.getAddressLine2(),
            contactInformation.getAddressLine3(),
            contactInformation.getTownCity(),
            contactInformation.getCounty(),
            contactInformation.getCountry(),
            contactInformation.getPostCode()
        );
    }
}
