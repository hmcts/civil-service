package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseLink {

    @JsonProperty(value = "CaseReference")
    private String caseReference;

}
