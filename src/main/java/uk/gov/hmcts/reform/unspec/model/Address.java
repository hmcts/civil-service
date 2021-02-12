package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.utils.ObjectUtils;

import static java.lang.String.join;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Address {

    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    private final String country;
    private final String postCode;

    @JsonIgnore
    public String firstNonNull() {
        return ObjectUtils.firstNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            join(", ", county, country)
        );
    }

    @JsonIgnore
    public String secondNonNull() {
        return ObjectUtils.secondNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            join(", ", county, country)
        );
    }

    @JsonIgnore
    public String thirdNonNull() {
        return ObjectUtils.thirdNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            join(", ", county, country)
        );
    }

    @JsonIgnore
    public String fourthNonNull() {
        return ObjectUtils.fourthNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            join(", ", county, country)
        );
    }

    @JsonIgnore
    public String fifthNonNull() {
        return ObjectUtils.fifthNonNull(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            join(", ", county, country)
        );
    }
}
