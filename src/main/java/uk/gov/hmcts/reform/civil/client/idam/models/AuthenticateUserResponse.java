package uk.gov.hmcts.reform.civil.client.idam.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUserResponse {

    private String code;

    @JsonCreator
    public AuthenticateUserResponse(@JsonProperty("code") String code) {
        this.code = code;
    }
}
