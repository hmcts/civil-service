package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
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