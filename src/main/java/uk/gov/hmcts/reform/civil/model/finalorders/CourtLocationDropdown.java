package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CourtLocationDropdown {

    private DynamicList finalOrderFurtherHearingCourtLocationList;

}
