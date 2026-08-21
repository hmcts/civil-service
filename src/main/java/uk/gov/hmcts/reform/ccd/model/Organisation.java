package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;

}
