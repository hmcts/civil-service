package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Builder

public class ClaimAmountBreakup {

    private ClaimAmountBreakupDetails value;
    @JsonIgnore
    private String id;
}
