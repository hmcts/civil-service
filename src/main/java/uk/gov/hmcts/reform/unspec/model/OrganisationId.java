package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class OrganisationId {

    @JsonProperty("OrganisationID")
    private String id;
}
