package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class SolicitorDetails {

    @JsonProperty("caseDataId")
    private String caseDataId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("caseRole")
    private String caseRole;
}
