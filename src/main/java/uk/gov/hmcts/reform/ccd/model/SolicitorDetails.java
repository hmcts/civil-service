package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder(toBuilder = true)
public class SolicitorDetails {

    @JsonProperty("caseDataId")
    private String caseDataId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("caseRole")
    private String caseRole;
}
