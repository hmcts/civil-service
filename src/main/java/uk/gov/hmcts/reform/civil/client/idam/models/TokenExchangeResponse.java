package uk.gov.hmcts.reform.civil.client.idam.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {
    private final String accessToken;

    @JsonCreator
    public TokenExchangeResponse(@JsonProperty("access_token") String accessToken) {
        this.accessToken = accessToken;
    }

}
