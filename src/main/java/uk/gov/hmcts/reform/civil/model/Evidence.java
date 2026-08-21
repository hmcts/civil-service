package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EvidenceList", generate = false)
@Data
@Accessors(chain = true)
public class Evidence {

    private EvidenceDetails value;
    @JsonIgnore
    private String id;
}
