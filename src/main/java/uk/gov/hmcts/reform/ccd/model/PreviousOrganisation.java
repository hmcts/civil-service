package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
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
