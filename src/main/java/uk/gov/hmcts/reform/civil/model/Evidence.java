package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Accessors(chain = true)
public class Evidence {

    private EvidenceDetails value;
    @JsonIgnore
    private String id;
}
