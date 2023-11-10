package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SmallClaimsWitnessStatement {

    private String input1;
    private String input2;
    private String input3;
    private String input4;
    private String text;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsNumberOfWitnessesToggle;
}
