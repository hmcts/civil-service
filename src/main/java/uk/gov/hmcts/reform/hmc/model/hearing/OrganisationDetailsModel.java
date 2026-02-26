package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationDetailsModel {

    private String name;
    private String organisationType;
    private String cftOrganisationID;
}
