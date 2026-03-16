package uk.gov.hmcts.reform.civil.client.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class GeneratePinResponse {
    private String pin;
    private String userId;

    public GeneratePinResponse() {
        super();
    }

    public GeneratePinResponse(String pin, String userId) {
        this.pin = pin;
        this.userId = userId;
    }
}
