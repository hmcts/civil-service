package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SmallClaimsWitnessStatement {

    private String input1;
    private String input2;
    private String input3;
    private String input4;
    private String text;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsNumberOfWitnessesToggle;
}
