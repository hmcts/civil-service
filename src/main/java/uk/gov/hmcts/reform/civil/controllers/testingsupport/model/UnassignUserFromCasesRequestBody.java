package uk.gov.hmcts.reform.civil.controllers.testingsupport.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class UnassignUserFromCasesRequestBody {

    @JsonProperty("caseIds")
    private List<String> caseIds;

}
