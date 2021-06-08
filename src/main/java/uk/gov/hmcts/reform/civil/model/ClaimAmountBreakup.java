package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.json.JSONPropertyIgnore;

@Data
@Builder

public class ClaimAmountBreakup {

    private final ClaimAmountBreakupDetails value;
    @JsonIgnore
    private final String id;
}
