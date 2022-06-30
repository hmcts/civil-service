package uk.gov.hmcts.reform.civil.model.referencedata.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JudgeSearchRequest {

    @JsonProperty("searchString")
    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    @Size(min = 3, message = "should have atleast {min} characters")
    @Pattern(regexp = "[a-zA-Z]+", message = "should contains letters only")
    private String searchString;

    @JsonProperty("serviceCode")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "should not be empty or contain special characters")
    private String serviceCode;

    @JsonProperty("location")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "should not be empty or contain special characters")
    private String location;

    public void setSearchString(String searchString) {
        this.searchString = searchString.trim();
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode != null ? serviceCode.trim().toLowerCase() : null;
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim().toLowerCase() : null;
    }
}
