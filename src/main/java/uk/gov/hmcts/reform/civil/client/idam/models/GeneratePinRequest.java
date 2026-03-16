package uk.gov.hmcts.reform.civil.client.idam.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GeneratePinRequest {
    private final String firstName;
    @JsonInclude
    private final String lastName;
    private final List<String> roles;

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = null;
    }
}

