package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class ChangeOfRepresentation {

    @CCD(label = "organisationToRemoveID", searchable = false)
    @JsonProperty("organisationToRemoveID")
    private String organisationToRemoveID;
    @CCD(label = "organisationToAddID", searchable = false)
    @JsonProperty("organisationToAddID")
    private String organisationToAddID;
    @CCD(label = "caseRole", searchable = false)
    @JsonProperty("caseRole")
    private String caseRole;
    @CCD(label = "timestamp", searchable = false)
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    @CCD(label = "formerRepresentationEmailAddress", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("formerRepresentationEmailAddress")
    private String formerRepresentationEmailAddress;

}