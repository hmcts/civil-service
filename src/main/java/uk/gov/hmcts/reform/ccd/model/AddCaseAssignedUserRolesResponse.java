package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddCaseAssignedUserRolesResponse {

    private String status;

    @JsonProperty("status_message")
    private String statusMessage;
}
