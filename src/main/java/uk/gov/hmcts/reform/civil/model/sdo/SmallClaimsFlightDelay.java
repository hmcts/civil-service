package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmallClaimsFlightDelay {

    private String relatedClaimsInput;
    private String legalDocumentsInput;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsFlightDelayToggle;
}
