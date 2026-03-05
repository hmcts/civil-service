package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClaimAmountBreakup {

    private ClaimAmountBreakupDetails value;
    @JsonIgnore
    private String id;
}