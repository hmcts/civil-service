package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

public class SmallClaimsHearing {

    private String input1;
    private SmallClaimsTimeEstimate time;
    private String input2;
}
