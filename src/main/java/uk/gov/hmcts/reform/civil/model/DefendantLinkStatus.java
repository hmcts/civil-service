package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefendantLinkStatus {

    @JsonProperty("isOcmcCase")
    private boolean isOcmcCase;
    private boolean linked;
}
