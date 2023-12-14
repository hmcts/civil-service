package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FastTrackClinicalNegligence {

    private String input1;
    private String input2;
    private String input3;
    private String input4;
}
