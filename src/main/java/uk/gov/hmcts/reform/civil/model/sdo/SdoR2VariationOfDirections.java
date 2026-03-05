package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2VariationOfDirections {

    private List<IncludeInOrderToggle> includeInOrderToggle;

}
