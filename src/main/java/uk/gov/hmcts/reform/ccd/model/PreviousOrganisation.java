package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Address;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class PreviousOrganisation {

    @JsonProperty("OrganisationName")
    private String organisationName;
    @JsonProperty("FromTimestamp")
    private LocalDateTime fromTimestamp;
    @JsonProperty("ToTimestamp")
    private LocalDateTime toTimestamp;
    @JsonProperty("OrganisationAddress")
    private Address organisationAddress;
}
