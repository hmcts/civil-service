package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationPolicy {

    @JsonProperty("Organisation")
    private OrganisationId organisation;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

}
