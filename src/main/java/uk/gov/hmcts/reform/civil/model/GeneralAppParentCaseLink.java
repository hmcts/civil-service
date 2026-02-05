package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralAppParentCaseLink {

    @JsonProperty(value = "CaseReference")
    private String caseReference;

}
