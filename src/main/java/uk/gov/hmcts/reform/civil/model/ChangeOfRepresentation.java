package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChangeOfRepresentation {

    @JsonProperty("organisationToRemoveID")
    private String organisationToRemoveID;
    @JsonProperty("organisationToAddID")
    private String organisationToAddID;
    @JsonProperty("caseRole")
    private String caseRole;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    @JsonProperty("formerRepresentationEmailAddress")
    private String formerRepresentationEmailAddress;

}
