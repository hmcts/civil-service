package uk.gov.hmcts.reform.migration.domain.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Address {
    @JsonProperty("AddressLine1")
    private final String addressLine1;
    @JsonProperty("AddressLine2")
    private final String addressLine2;
    @JsonProperty("AddressLine3")
    private final String addressLine3;
    @JsonProperty("PostTown")
    private final String postTown;
    @JsonProperty("County")
    private final String county;
    @JsonProperty("PostCode")
    private final String postcode;
    @JsonProperty("Country")
    private final String country;
}
